package team.rescue.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import team.rescue.auth.dto.JoinDto.EmailConfirmDto;
import team.rescue.auth.dto.JoinDto.JoinReqDto;
import team.rescue.auth.dto.JoinDto.JoinResDto;
import team.rescue.auth.service.AuthService;
import team.rescue.auth.type.ProviderType;
import team.rescue.auth.type.RoleType;
import team.rescue.member.dto.MemberDto.MemberInfoDto;
import team.rescue.member.entity.Member;
import team.rescue.member.repository.MemberRepository;
import team.rescue.mock.MockMember;
import team.rescue.mock.WithMockMember;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@ActiveProfiles(profiles = "test")
@Transactional
class AuthControllerTest extends MockMember {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MemberRepository memberRepository;

	@MockBean
	private AuthService authService;

	private Member existMember;

	@BeforeEach
	public void setup() {
		// 기존 유저
		this.existMember = memberRepository.save(
				getNewMember("test", ProviderType.EMAIL, RoleType.GUEST)
		);
	}

	@Test
	@DisplayName("이메일 회원 가입 성공 - 인증 메일이 전송 되고, 인증 전까지 유저는 GUEST 권한을 갖는다.")
	public void email_join_success() throws Exception {

		// given
		JoinReqDto joinReqDto = new JoinReqDto();
		joinReqDto.setNickname("member");
		joinReqDto.setEmail("member@gmail.com");
		joinReqDto.setPassword("1234567890");

		String requestBody = objectMapper.writeValueAsString(joinReqDto);

		// Stub: 이메일 회원가입 정상 동작
		Member member = getNewMember(
				joinReqDto.getNickname(),
				ProviderType.EMAIL,
				RoleType.GUEST
		);
		JoinResDto joinResDto = new JoinResDto(member);
		given(authService.createEmailUser(any(JoinReqDto.class))).willReturn(joinResDto);

		// when
		ResultActions resultActions = mockMvc.perform(
				post("/api/auth/email/join").content(requestBody)
						.contentType(MediaType.APPLICATION_JSON));

		System.out.println(resultActions.andReturn().getResponse().getContentAsString());

		// then
		// Status == 201 Created
		resultActions.andExpect(status().isCreated());
		// Response Body
		resultActions.andExpect(jsonPath("$.data.nickname").value(joinReqDto.getNickname()));
		resultActions.andExpect(jsonPath("$.data.email").value(joinReqDto.getEmail()));
		resultActions.andExpect(jsonPath("$.data.role").value(RoleType.GUEST.name()));
	}

	@Test
	@DisplayName("이메일 인증 성공")
	@WithMockMember
	public void confirm_email_code() throws Exception {

		// given
		EmailConfirmDto emailConfirmDto = new EmailConfirmDto();
		emailConfirmDto.setCode("123456");
		String requestBody = objectMapper.writeValueAsString(emailConfirmDto);

		// Stub: 이메일 인증 완료
		this.existMember.updateRole(RoleType.USER);
		MemberInfoDto memberInfoDto = MemberInfoDto.of(existMember);
		given(authService.confirmEmailCode(anyString(), anyString()))
				.willReturn(memberInfoDto);

		// when
		ResultActions resultActions = mockMvc.perform(
				post("/api/auth/email/confirm").content(requestBody)
						.contentType(MediaType.APPLICATION_JSON));

		System.out.println(resultActions.andReturn().getResponse().getContentAsString());

		// then
		// Status == 200 OK
		resultActions.andExpect(status().isOk());
		resultActions.andExpect(jsonPath("$.data.id").value(existMember.getId()));
		resultActions.andExpect(jsonPath("$.data.nickname").value(existMember.getNickname()));
		// Role == USER
		resultActions.andExpect(jsonPath("$.data.role").value(RoleType.USER.name()));
	}

	@Test
	@DisplayName("회원 탈퇴 성공")
	@WithMockMember(role = RoleType.USER)
	void successDeleteMember() throws Exception {
		// given
		doNothing().when(authService).disableMember("test@gmail.com");

		// when
		// then
		mockMvc.perform(delete("/api/auth/leave"))
				.andExpect(jsonPath("$.message").value("회원 탈퇴가 정상적으로 처리되었습니다."))
				.andExpect(status().isOk())
				.andDo(print());
	}

	@Test
	@DisplayName("로그아웃 성공")
	@WithMockMember(role = RoleType.USER)
	void successLogout() throws Exception {
		// given
		doNothing().when(authService).logout("test@gmail.com");

		// when
		// then
		mockMvc.perform(get("/api/auth/logout"))
				.andExpect(jsonPath("$.message").value("로그아웃이 완료되었습니다."))
				.andExpect(status().isOk())
				.andDo(print());
	}
}
