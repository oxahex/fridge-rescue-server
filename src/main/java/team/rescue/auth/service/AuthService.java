package team.rescue.auth.service;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.rescue.auth.dto.JoinDto.JoinReqDto;
import team.rescue.auth.dto.JoinDto.JoinResDto;
import team.rescue.auth.dto.TokenDto;
import team.rescue.auth.provider.JwtTokenProvider;
import team.rescue.auth.provider.MailProvider;
import team.rescue.auth.type.JwtTokenType;
import team.rescue.auth.type.ProviderType;
import team.rescue.auth.type.RoleType;
import team.rescue.auth.user.PrincipalDetails;
import team.rescue.common.redis.RedisPrefix;
import team.rescue.common.redis.RedisRepository;
import team.rescue.error.exception.AuthException;
import team.rescue.error.exception.ServiceException;
import team.rescue.error.type.AuthError;
import team.rescue.error.type.ServiceError;
import team.rescue.fridge.repository.FridgeRepository;
import team.rescue.fridge.service.FridgeService;
import team.rescue.member.entity.Member;
import team.rescue.member.repository.MemberRepository;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

	private final static String ENCODING = "UTF-8";

	private final MailProvider mailProvider;
	private final PasswordEncoder passwordEncoder;
	private final FridgeService fridgeService;
	private final MemberRepository memberRepository;
	private final FridgeRepository fridgeRepository;
	private final RedisRepository redisRepository;


	@Override
	public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
		log.debug("[+] loadUserByUsername start");
		Member member = memberRepository.findUserByEmail(userEmail)
				.orElseThrow(() -> new ServiceException(ServiceError.USER_NOT_FOUND));

		// 탈퇴한 회원 로그인 시도 시
		if (!member.getIsEnabled()) {
			throw new ServiceException(ServiceError.USER_ALREADY_LEAVE);
		}

		log.debug(member.getEmail());
		return new PrincipalDetails(member);
	}

	/**
	 * 유저 생성
	 * <p> 사용자 Name, Email, Password 를 받아 새로운 유저 생성
	 * <p> Email 중복 여부 검증
	 * <p> 인증 Email 전송
	 *
	 * @param joinReqDto Email 회원가입 요청 DTO
	 * @return 생성된 User Entity
	 */
	@Transactional
	public JoinResDto createEmailUser(JoinReqDto joinReqDto) {

		log.info("[Email 회원 가입] email={}", joinReqDto.getEmail());

		// Email 중복 검증
		validateCreateMember(joinReqDto.getEmail());

		Member member = Member.builder()
				.nickname(joinReqDto.getNickname())
				.email(joinReqDto.getEmail())
				.password(passwordEncoder.encode(joinReqDto.getPassword()))
				.role(RoleType.GUEST)
				.provider(ProviderType.EMAIL)
				.isEnabled(true)
				.build();

		// 인증 Email 전송 및 DTO 반환
		return new JoinResDto(sendConfirmEmail(member));
	}

	/**
	 * 인증 이메일 전송
	 *
	 * @param member 전송할 유저
	 * @return 이메일 인증 코드 업데이트 처리 된 유저
	 */
	@Transactional
	public Member sendConfirmEmail(Member member) {

		log.info("[인증 메일 전송] email={}", member.getEmail());

		// 인증 코드 생성 및 전송
		String emailCode = mailProvider.sendEmail(member);
		// 인증 코드 Redis 저장
		redisRepository.put(RedisPrefix.CODE, member.getEmail(), emailCode);

		log.info("[인증 메일 전송 완료]");

		return memberRepository.save(member);
	}

	/**
	 * 이메일 코드 인증
	 *
	 * @param email 로그인 유저 이메일
	 * @param code  유저 입력 이메일 코드
	 * @return JWT Access Token
	 */
	@Transactional
	public String confirmEmailCode(String email, String code) {

		Member member = memberRepository.findUserByEmail(email)
				.orElseThrow(() -> new ServiceException(ServiceError.USER_NOT_FOUND));

		// Validate Email Code
		validateEmailCode(member, code);

		// Role Update
		member.updateRole(RoleType.USER);

		// 냉장고 생성
		member.registerFridge(fridgeService.createFridge(member));

		// 새 인증 정보 생성
		PrincipalDetails principalDetails = new PrincipalDetails(member);

		return JwtTokenProvider.createToken(principalDetails, JwtTokenType.ACCESS_TOKEN);
	}

	/**
	 * 유저 입력 이메일 코드와 실제 이메일 코드 일치 여부 확인
	 *
	 * @param member 요청 유저
	 * @param code   유저 입력 이메일 코드
	 */
	private void validateEmailCode(Member member, String code) {

		String key = RedisPrefix.CODE.name() + member.getEmail();
		String emailCode = redisRepository.get(key);

		if (!Objects.equals(emailCode, code)) {
			throw new ServiceException(ServiceError.EMAIL_CODE_MIS_MATCH);
		}

		// 일치하는 경우 삭제
		redisRepository.delete(key);
	}

	/**
	 * 회원 생성 검증
	 * <p> 이메일 중복 불가
	 *
	 * @param email 검증할 이메일
	 */
	private void validateCreateMember(String email) {

		if (memberRepository.existsByEmail(email)) {
			throw new ServiceException(ServiceError.EMAIL_ALREADY_EXIST);
		}
	}

	/**
	 * 회원 탈퇴
	 *
	 * @param email 사용자 이메일
	 */
	@Transactional
	public void disableMember(String email) {
		Member member = memberRepository.findUserByEmail(email)
				.orElseThrow(() -> new ServiceException(ServiceError.USER_NOT_FOUND));

		// 이미 탈퇴한 회원인 경우
		if (!member.getIsEnabled()) {
			throw new ServiceException(ServiceError.USER_ALREADY_LEAVE);
		}

		// 삭제 유저 처리
		member.leave();
	}

	/**
	 * accessToken 재발급
	 *
	 * @param refreshToken
	 * @param principalDetails
	 */
	@Transactional
	public TokenDto reissueToken(String refreshToken, PrincipalDetails principalDetails) {
		String cachedRefreshToken = redisRepository.get(principalDetails.getUsername());

		String savedToken =
				cachedRefreshToken == null ? getTokenFromDB(principalDetails) : cachedRefreshToken;

		// refreshToken 불일치 -> 다시 로그인 필요 (재로그인을 하면 accessToken, refreshToken 갱신)
		if (!Objects.equals(refreshToken, savedToken)) {
			throw new AuthException(AuthError.ACCESS_DENIED);
		}

		String accessToken = JwtTokenProvider.createToken(principalDetails, JwtTokenType.ACCESS_TOKEN);
		return new TokenDto(accessToken, refreshToken);
	}

	/**
	 * DB에 있는 refreshToken 값 가져오기 위한 메서드
	 *
	 * @param principalDetails 사용자 정보
	 */
	private String getTokenFromDB(PrincipalDetails principalDetails) {
		Member member = memberRepository.findUserByEmail(principalDetails.getUsername())
				.orElseThrow(() -> new ServiceException(ServiceError.USER_NOT_FOUND));

		return member.getToken();
	}

	@Transactional
	public void logout(String email) {
		Member member = memberRepository.findUserByEmail(email)
				.orElseThrow(() -> new ServiceException(ServiceError.USER_NOT_FOUND));

		member.updateToken(null);
		redisRepository.delete(email);
	}
}
