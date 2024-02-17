package team.rescue.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import team.rescue.auth.dto.JoinDto.JoinReqDto;
import team.rescue.auth.dto.JoinDto.JoinResDto;
import team.rescue.auth.provider.MailProvider;
import team.rescue.auth.type.ProviderType;
import team.rescue.auth.type.RoleType;
import team.rescue.error.exception.ServiceException;
import team.rescue.error.type.ServiceError;
import team.rescue.fridge.entity.Fridge;
import team.rescue.fridge.repository.FridgeRepository;
import team.rescue.fridge.service.FridgeService;
import team.rescue.member.dto.MemberDto.MemberInfoDto;
import team.rescue.member.entity.Member;
import team.rescue.member.repository.MemberRepository;
import team.rescue.mock.WithMockMember;
import team.rescue.util.RedisUtil;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@InjectMocks
	AuthService authService;

	@Mock
	FridgeService fridgeService;

	@Mock
	MemberRepository memberRepository;

	@Mock
	FridgeRepository fridgeRepository;

	@Mock
	MailProvider mailProvider;

	@Mock
	RedisUtil redisUtil;

	@Spy
	private PasswordEncoder passwordEncoder;

	@Test
	@DisplayName("이메일 회원 가입 성공 - 이메일 회원 가입 시 GUEST 권한 유저를 반환한다.")
	public void email_join_success() throws Exception {

		// given
		JoinReqDto joinReqDto = new JoinReqDto();
		joinReqDto.setNickname("member");
		joinReqDto.setEmail("member@gmail.com");
		joinReqDto.setPassword("1234567890");

		// Stub 1: 이전에 가입한 적 없는 이메일
		given(memberRepository.existsByEmail(anyString()))
				.willReturn(false);

		// Stub 2: 이메일 코드 생성 및 전송 완료
		Member member = Member.builder()
				.nickname(joinReqDto.getNickname())
				.email(joinReqDto.getEmail())
				.password(passwordEncoder.encode(joinReqDto.getPassword()))
				.role(RoleType.GUEST)
				.provider(ProviderType.EMAIL)
				.build();

		// when
		given(memberRepository.save(any())).willReturn(member);
		JoinResDto joinResDto = authService.createEmailUser(joinReqDto);

		// then
		// GUEST 권한 유저 생성
		assertEquals(RoleType.GUEST, joinResDto.getRole());
		assertEquals(member.getEmail(), joinResDto.getEmail());
	}

	@Test
	@DisplayName("이메일 회원 가입 실패 - 이미 가입된 이메일로 다시 가입할 수 없다.")
	public void create_email_user_failure_already_exist_email() throws Exception {

		// given
		JoinReqDto joinReqDto = new JoinReqDto();
		joinReqDto.setNickname("member");
		joinReqDto.setEmail("member@gmail.com");
		joinReqDto.setPassword("1234567890");

		given(memberRepository.existsByEmail(anyString()))
				.willReturn(true);

		// when
		ServiceException exception = assertThrows(ServiceException.class,
				() -> authService.createEmailUser(joinReqDto));

		// then
		assertEquals(ServiceError.EMAIL_ALREADY_EXIST.getHttpStatus(),
				exception.getStatusCode());
		assertEquals(ServiceError.EMAIL_ALREADY_EXIST.getErrorMessage(),
				exception.getErrorMessage());
	}


	@Test
	@DisplayName("이메일 인증 메일 전송 성공")
	public void send_confirm_email() throws Exception {

		// given
		Member member = Member.builder()
				.nickname("nickname")
				.email("test@gmail.com")
				.password(passwordEncoder.encode("1234567890"))
				.role(RoleType.GUEST)
				.provider(ProviderType.EMAIL)
				.build();

		// Stub 1: Email Code 생성
		String emailCode = "123456";
		given(mailProvider.sendEmail(any(Member.class)))
				.willReturn(emailCode);

		// Stub 2: 멤버 객체 저장
		given(memberRepository.save(any(Member.class)))
				.willReturn(member);

		// when
		Member savedMember = authService.sendConfirmEmail(member);

		// then
		// Email Code 저장
		assertNotNull(savedMember.getEmailCode());
		assertEquals(emailCode, savedMember.getEmailCode());
	}

	@Test
	@DisplayName("이메일 코드 인증 성공 - 유저의 권한이 USER로 업데이트 되고, 냉장고가 생성된다.")
	public void confirm_email_code_success() throws Exception {

		// given
		String email = "test@gmail.com";
		String code = "123456";
		Member member = Member.builder()
				.nickname("nickname")
				.email("test@gmail.com")
				.password(passwordEncoder.encode("1234567890"))
				.emailCode("123456")
				.role(RoleType.GUEST)
				.provider(ProviderType.EMAIL)
				.build();

		// Stub 1: 해당 유저가 존재
		given(memberRepository.findUserByEmail(anyString()))
				.willReturn(Optional.of(member));

		// Stub 2: 냉장고 생성 로직 수행
		given(fridgeService.createFridge(any(Member.class)))
				.willReturn(Fridge.builder()
						.member(member)
						.build());

		// when
		MemberInfoDto memberInfoDto = authService.confirmEmailCode(email, code);

		// then
		// 유저 권한이 GUEST 가 아니라 USER로 업데이트 된다.
		assertEquals(RoleType.USER, memberInfoDto.getRole());
	}

	@Test
	@DisplayName("이메일 코드 인증 실패 - 해당 이메일로 가입한 유저가 없는 경우 이메일 인증이 불가능하다.")
	public void confirm_email_code_failure_not_found_user() throws Exception {

		// given
		String email = "test@gmail.com";
		String code = "123456";
		Member member = Member.builder()
				.nickname("nickname")
				.email("test@gmail.com")
				.password(passwordEncoder.encode("1234567890"))
				.emailCode("123456")
				.role(RoleType.GUEST)
				.provider(ProviderType.EMAIL)
				.build();

		// Stub 1: 해당 이메일로 가입한 유저 없음
		given(memberRepository.findUserByEmail(anyString()))
				.willReturn(Optional.empty());

		// when
		ServiceException exception = assertThrows(ServiceException.class,
				() -> authService.confirmEmailCode(email, code));

		// then
		assertEquals(ServiceError.USER_NOT_FOUND.getHttpStatus(),
				exception.getStatusCode());
		assertEquals(ServiceError.USER_NOT_FOUND.getErrorMessage(),
				exception.getErrorMessage());
	}

	@Test
	@DisplayName("이메일 코드 인증 실패 - 이메일 인증 코드가 불일치하는 경우 이메일 인증이 불가하다.")
	public void confirm_email_code_failure_mis_match_code() throws Exception {

		// given
		String email = "test@gmail.com";
		String code = "123456";

		// Stub 1: 해당 이메일로 가입한 유저 있음
		// Stub 2: 저장된 이메일 코드와 확인 요청한 코드가 다름
		Member member = Member.builder()
				.nickname("nickname")
				.email("test@gmail.com")
				.password(passwordEncoder.encode("1234567890"))
				.emailCode("654321")
				.role(RoleType.GUEST)
				.provider(ProviderType.EMAIL)
				.build();
		given(memberRepository.findUserByEmail(anyString()))
				.willReturn(Optional.of(member));

		// when
		ServiceException exception = assertThrows(ServiceException.class,
				() -> authService.confirmEmailCode(email, code));

		// then
		assertEquals(ServiceError.EMAIL_CODE_MIS_MATCH.getHttpStatus(),
				exception.getStatusCode());
		assertEquals(ServiceError.EMAIL_CODE_MIS_MATCH.getErrorMessage(),
				exception.getErrorMessage());
	}

	@Test
	@DisplayName("회원 탈퇴 성공")
	@WithMockMember(role = RoleType.USER)
	void successDeleteMember() {
		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트")
				.email("test@gmail.com")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		// when
		authService.disableMember("test@gmail.com");

		// then
		verify(fridgeRepository, times(1)).deleteByMember(any());
		verify(memberRepository, times(1)).deleteById(anyLong());
	}

	@Test
	@DisplayName("회원 탈퇴 실패 - 사용자 정보 없음")
	@WithMockMember(role = RoleType.USER)
	void failDeleteMember_UserNotFound() {
		// given
		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> authService.disableMember("test@gmail.com"));

		// then
		assertEquals(ServiceError.USER_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("로그아웃 성공")
	@WithMockMember(role = RoleType.USER)
	void successLogout() {
		// given
		Member member = Member.builder()
				.id(1L)
				.email("test@gmail.com")
				.token("asdfasdfasdf")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));
		// when
		authService.logout("test@gmail.com");

		// then
		assertNull(member.getToken());
		assertNull(redisUtil.get("test@gmail.com"));
	}

	@Test
	@DisplayName("로그아웃 실패 - 사용자 정보 없음")
	@WithMockMember(role = RoleType.USER)
	void failLogout() {
		// given
		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> authService.logout("test@gmail.com"));

		// then
		assertEquals(ServiceError.USER_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}
}
