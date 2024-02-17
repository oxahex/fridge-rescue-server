package team.rescue.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.rescue.auth.dto.JoinDto;
import team.rescue.auth.dto.JoinDto.JoinResDto;
import team.rescue.auth.dto.TokenDto;
import team.rescue.auth.service.AuthService;
import team.rescue.auth.type.ProviderType;
import team.rescue.auth.user.PrincipalDetails;
import team.rescue.common.dto.ResponseDto;
import team.rescue.member.dto.MemberDto.MemberInfoWithTokenDto;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private static final String HEADER_REFRESH_TOKEN = "Refresh-Token";
	private static final String TOKEN_PREFIX = "Bearer ";

	private final AuthService authService;

	/**
	 * Email 회원 가입
	 *
	 * @param joinReqDto 이메일 가입 시 입력값
	 * @return GUEST 회원 정보
	 */
	@PostMapping("/email/join")
	@PreAuthorize("permitAll()")
	public ResponseEntity<ResponseDto<JoinResDto>> emailJoin(
			@RequestBody @Valid JoinDto.JoinReqDto joinReqDto,
			BindingResult bindingResult
	) {

		log.info("[이메일 회원 가입] email={}", joinReqDto.getEmail());

		JoinResDto joinResDto = authService.createEmailUser(joinReqDto);

		return new ResponseEntity<>(
				new ResponseDto<>("회원 가입이 완료되었습니다. 이메일 코드를 인증해주세요.", joinResDto),
				HttpStatus.CREATED
		);
	}

	/**
	 * 이메일 인증 코드 확인
	 *
	 * @param emailConfirmDto 이메일 인증 요청
	 * @return 확인 여부 반환
	 */
	@PostMapping("/email/confirm")
	@PreAuthorize("permitAll()")
	public ResponseEntity<ResponseDto<MemberInfoWithTokenDto>> emailConfirm(
			@RequestBody @Valid JoinDto.EmailConfirmDto emailConfirmDto,
			BindingResult bindingResult,
			@AuthenticationPrincipal PrincipalDetails details
	) {

		log.info("[이메일 코드 확인] code={}", emailConfirmDto.getCode());

		MemberInfoWithTokenDto memberInfoDto = null;

		if (details == null) {
			memberInfoDto = authService.confirmEmailCode(emailConfirmDto.getEmail(),
					emailConfirmDto.getCode());
		} else {
			memberInfoDto = authService
					.confirmEmailCode(details.getUsername(), emailConfirmDto.getCode());
		}
		return new ResponseEntity<>(
				new ResponseDto<>(null, memberInfoDto), HttpStatus.OK
		);
	}

	/**
	 * OAuth 회원 가입/로그인
	 *
	 * @param response OAuth Type 별 리다이렉트
	 * @param provider OAuth Provider Type
	 */
	@GetMapping("/oauth")
	@PreAuthorize("permitAll()")
	public void oAuthLoginOrJoin(
			HttpServletResponse response,
			@RequestParam ProviderType provider
	) throws IOException {

		if (provider == ProviderType.GOOGLE) {
			log.info("[Google 회원 가입]");
			response.sendRedirect("/oauth2/authorization/google");
		}
	}

	/**
	 * 회원 탈퇴
	 *
	 * @param principalDetails 사용자 정보
	 */
	@DeleteMapping("/leave")
	@PreAuthorize("hasAuthority('USER') or hasAuthority('GUEST')")
	public ResponseEntity<ResponseDto<?>> deleteMember(
			@AuthenticationPrincipal PrincipalDetails principalDetails) {

		String email = principalDetails.getUsername();

		authService.disableMember(email);

		return ResponseEntity.ok(new ResponseDto<>("회원 탈퇴가 정상적으로 처리되었습니다.", null));
	}

	/**
	 * access token 재발급
	 *
	 * @param refreshToken     refreshToken
	 * @param principalDetails 사용자 정보
	 */
	@PostMapping("/token/reissue")
	@PreAuthorize("hasAuthority('USER') or hasAuthority('GUEST')")
	public ResponseEntity<ResponseDto<String>> reissueToken(
			@RequestHeader("Authorization") String refreshToken,
			@AuthenticationPrincipal PrincipalDetails principalDetails) {

		log.debug("Refresh Token : {}", refreshToken);

		TokenDto tokenDto = authService.reissueToken(refreshToken.substring(TOKEN_PREFIX.length()),
				principalDetails);

		HttpHeaders headers = new HttpHeaders();
		headers.set(HEADER_REFRESH_TOKEN, tokenDto.getRefreshToken());

		return new ResponseEntity<>(
				new ResponseDto<>("Access Token이 재발급되었습니다.", tokenDto.getAccessToken()),
				headers,
				HttpStatus.OK
		);
	}

	@GetMapping("/logout")
	@PreAuthorize("hasAuthority('USER') or hasAuthority('GUEST')")
	public ResponseEntity<ResponseDto<?>> logout(
			@AuthenticationPrincipal PrincipalDetails principalDetails) {

		authService.logout(principalDetails.getUsername());

		return ResponseEntity.ok(new ResponseDto<>("로그아웃이 완료되었습니다.", null));
	}
}
