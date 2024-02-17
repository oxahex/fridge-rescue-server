package team.rescue.auth.handler;

import static team.rescue.auth.type.JwtTokenType.ACCESS_TOKEN;
import static team.rescue.auth.type.JwtTokenType.REFRESH_TOKEN;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import team.rescue.auth.dto.LoginDto.LoginResDto;
import team.rescue.auth.provider.JwtTokenProvider;
import team.rescue.auth.user.PrincipalDetails;
import team.rescue.error.exception.ServiceException;
import team.rescue.error.type.ServiceError;
import team.rescue.member.entity.Member;
import team.rescue.member.repository.MemberRepository;
import team.rescue.util.RedisUtil;

@Slf4j
@RequiredArgsConstructor
public class OAuthAuthorizationSuccessHandler implements AuthenticationSuccessHandler {

	private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24; // 24h
	private static final String TOKEN_PREFIX = "Bearer ";
	private static final String HEADER_ACCESS_TOKEN = "Access-Token";
	private static final String HEADER_REFRESH_TOKEN = "Refresh-Token";

	private final RedisUtil redisUtil;
	private final MemberRepository memberRepository;

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication
	) throws IOException, ServletException {

		PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
		log.info(principalDetails.getAttributes().toString());

		String accessToken = JwtTokenProvider.createToken(principalDetails, ACCESS_TOKEN);
		String refreshToken = JwtTokenProvider.createToken(principalDetails, REFRESH_TOKEN);

		log.debug("accessToken = {}", accessToken);
		log.debug("refreshToken = {}", refreshToken);

		saveRefreshToken(principalDetails, refreshToken);

		redisUtil.put(principalDetails.getUsername(), refreshToken, REFRESH_TOKEN_EXPIRE_TIME);

		// access token은 Response Body에 담아서 클라이언트에게 전달
		LoginResDto loginResponse = new LoginResDto(principalDetails.getMember(), accessToken);

		response.setStatus(HttpStatus.OK.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		// refresh token을 Header에 담아서 클라이언트에게 전달
		response.setHeader(HEADER_REFRESH_TOKEN, refreshToken);

		new ObjectMapper().writeValue(response.getOutputStream(), loginResponse);
	}

	private void saveRefreshToken(PrincipalDetails principalDetails, String refreshToken) {
		Member member = memberRepository.findUserByEmail(principalDetails.getUsername())
				.orElseThrow(() -> new ServiceException(ServiceError.USER_NOT_FOUND));

		member.updateToken(refreshToken);
		memberRepository.save(member);
	}
}
