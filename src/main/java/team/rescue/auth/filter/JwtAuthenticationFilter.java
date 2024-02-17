package team.rescue.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import team.rescue.auth.dto.LoginDto.LoginReqDto;
import team.rescue.auth.dto.LoginDto.LoginResDto;
import team.rescue.auth.provider.JwtTokenProvider;
import team.rescue.auth.type.JwtTokenType;
import team.rescue.auth.type.RoleType;
import team.rescue.auth.user.PrincipalDetails;
import team.rescue.common.redis.RedisPrefix;
import team.rescue.common.redis.RedisRepository;
import team.rescue.error.exception.AuthException;
import team.rescue.error.exception.ServiceException;
import team.rescue.error.type.AuthError;
import team.rescue.error.type.ServiceError;
import team.rescue.fridge.entity.Fridge;
import team.rescue.fridge.entity.FridgeIngredient;
import team.rescue.fridge.repository.FridgeIngredientRepository;
import team.rescue.fridge.repository.FridgeRepository;
import team.rescue.member.entity.Member;
import team.rescue.member.repository.MemberRepository;
import team.rescue.notification.entity.NotificationProperty;
import team.rescue.notification.event.NotificationEvent;
import team.rescue.notification.event.NotificationEventPublisher;
import team.rescue.notification.type.NotificationType;

@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private static final String LOGIN_PATH = "/api/auth/email/login";
	private static final String TOKEN_PREFIX = "Bearer ";
	private static final String HEADER_ACCESS_TOKEN = "Access-Token";
	private static final String HEADER_REFRESH_TOKEN = "Refresh-Token";
	private final ObjectMapper objectMapper;
	private final AuthenticationManager authenticationManager;
	private final RedisRepository redisUtil;
	private final MemberRepository memberRepository;
	private final NotificationEventPublisher notificationEventPublisher;
	private final FridgeRepository fridgeRepository;
	private final FridgeIngredientRepository fridgeIngredientRepository;

	public JwtAuthenticationFilter(
			AuthenticationManager authenticationManager,
			ObjectMapper objectMapper,
			RedisRepository redisUtil,
			MemberRepository memberRepository,
			NotificationEventPublisher notificationEventPublisher,
			FridgeRepository fridgeRepository,
			FridgeIngredientRepository fridgeIngredientRepository
	) {
		setFilterProcessesUrl(LOGIN_PATH);
		this.authenticationManager = authenticationManager;
		this.objectMapper = objectMapper;
		this.redisUtil = redisUtil;
		this.memberRepository = memberRepository;
		this.notificationEventPublisher = notificationEventPublisher;
		this.fridgeRepository = fridgeRepository;
		this.fridgeIngredientRepository = fridgeIngredientRepository;
	}

	@Override
	public Authentication attemptAuthentication(
			HttpServletRequest request, HttpServletResponse response
	) throws AuthenticationException {

		log.debug("로그인 시도 : {}", request.getRequestURL());

		try {
			LoginReqDto loginReqDto = objectMapper.readValue(request.getInputStream(),
					LoginReqDto.class);
			log.debug("이메일: {}, 비밀번호: {}", loginReqDto.getEmail(), loginReqDto.getPassword());

			UsernamePasswordAuthenticationToken authRequestToken =
					new UsernamePasswordAuthenticationToken(loginReqDto.getEmail(),
							loginReqDto.getPassword());

			return authenticationManager.authenticate(authRequestToken);

		} catch (Exception e) {
			throw new AuthException(AuthError.AUTHENTICATION_FAILURE);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request,
			HttpServletResponse response,
			FilterChain chain,
			Authentication authentication) throws IOException {

		log.debug("JwtAuthenticationFilter.successfulAuthentication request={}",
				request.getRequestURI());

		// 로그인에 성공한 유저
		final PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

		// access token 생성
		String accessToken = JwtTokenProvider.createToken(principalDetails, JwtTokenType.ACCESS_TOKEN);

		// refresh token 생성
		String refreshToken = JwtTokenProvider.createToken(principalDetails,
				JwtTokenType.REFRESH_TOKEN);

		saveRefreshToken(principalDetails, refreshToken);

		// redis 저장
		redisUtil.put(
				RedisPrefix.TOKEN,
				principalDetails.getUsername(),
				refreshToken
		);

		// access token을 Response Body에 담아서 클라이언트에게 전달
		LoginResDto loginResponse = new LoginResDto(principalDetails.getMember(), accessToken);

		response.setStatus(HttpStatus.OK.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		// refresh token을 Header에 담아서 클라이언트에게 전달
		response.setHeader(HEADER_REFRESH_TOKEN, refreshToken);

		validateFridgeIngredientsExpirationDate(principalDetails);

		new ObjectMapper().writeValue(response.getOutputStream(), loginResponse);

	}

	private void validateFridgeIngredientsExpirationDate(PrincipalDetails principalDetails) {

		if (principalDetails.getMember().getRole() == RoleType.GUEST) {
			return;
		}

		Fridge fridge = fridgeRepository.findByMember(principalDetails.getMember())
				.orElseThrow(() -> new ServiceException(ServiceError.FRIDGE_NOT_FOUND));

		List<FridgeIngredient> fridgeIngredients = fridgeIngredientRepository.findByFridge(fridge);

		LocalDate today = LocalDate.now();

		for (FridgeIngredient fridgeIngredient : fridgeIngredients) {
			if (fridgeIngredient.getExpiredAt() != null && today.plusDays(1)
					.isEqual(fridgeIngredient.getExpiredAt())) {
				NotificationEvent event = NotificationEvent.builder()
						.email(principalDetails.getMember().getEmail())
						.notificationType(NotificationType.INGREDIENT_EXPIRED)
						.notificationProperty(NotificationProperty.builder()
								.contents(fridgeIngredient.getName() + "의 유통기한이 곧 만료됩니다. 냉장고를 확인하세요!")
								.originId(fridgeIngredient.getId())
								.originUserId(null)
								.build())
						.build();
				notificationEventPublisher.publishEvent(event);
			}
		}
	}

	/**
	 * 로그인 인증 실패 시 호출되는 메서드
	 */
	@Override
	protected void unsuccessfulAuthentication(
			HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
			throws IOException, ServletException {

		log.error("unsuccessfulAuthentication failed.getLocalizedMessage(): {}",
				failed.getLocalizedMessage());

		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("code", HttpStatus.UNAUTHORIZED.value());
		body.put("error", failed.getMessage());

		new ObjectMapper().writeValue(response.getOutputStream(), body);
	}

	private void saveRefreshToken(PrincipalDetails principalDetails, String refreshToken) {
		Member member = memberRepository.findUserByEmail(principalDetails.getUsername())
				.orElseThrow(() -> new ServiceException(ServiceError.USER_NOT_FOUND));

		member.updateToken(refreshToken);
		memberRepository.save(member);
	}
}
