package team.rescue.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import team.rescue.auth.type.RoleType;
import team.rescue.cook.dto.CookDto.CookInfoDto;
import team.rescue.member.dto.MemberDto.MemberDetailDto;
import team.rescue.member.dto.MemberDto.MemberNicknameUpdateDto;
import team.rescue.member.dto.MemberDto.MemberPasswordUpdateDto;
import team.rescue.member.repository.MemberRepository;
import team.rescue.member.service.MemberService;
import team.rescue.mock.MockMember;
import team.rescue.mock.WithMockMember;
import team.rescue.recipe.dto.RecipeDto.RecipeDetailDto;
import team.rescue.recipe.dto.RecipeDto.RecipeInfoDto;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
@Transactional
class MemberControllerTest extends MockMember {

	@MockBean
	MemberService memberService;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	MockMvc mockMvc;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	MemberRepository memberRepository;

	@Test
	@DisplayName("회원 정보 조회 성공")
	@WithMockMember(role = RoleType.USER)
	void successGetMemberInfo() throws Exception {
		// given
		MemberDetailDto memberDetailDto = MemberDetailDto.builder()
				.id(1L)
				.nickname("테스트")
				.email("test@gmail.com")
				.build();

		given(memberService.getMemberInfo("test@gmail.com"))
				.willReturn(memberDetailDto);

		// when
		// then
		mockMvc.perform(get("/api/members/info"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(1L))
				.andExpect(jsonPath("$.data.nickname").value("테스트"))
				.andExpect(jsonPath("$.data.email").value("test@gmail.com"))
				.andDo(print());
	}

	@Test
	@DisplayName("회원 닉네임 변경 성공")
	@WithMockMember(role = RoleType.USER)
	void successUpdateMemberNickname() throws Exception {
		// given
		MemberNicknameUpdateDto memberNicknameUpdateDto = MemberNicknameUpdateDto.builder()
				.nickname("테스트2")
				.build();

		MemberDetailDto memberDetailDto = MemberDetailDto.builder()
				.id(1L)
				.nickname("테스트2")
				.email("test@gmail.com")
				.build();

		given(memberService.updateMemberNickname("test@gmail.com", memberNicknameUpdateDto))
				.willReturn(memberDetailDto);

		// when
		// then
		mockMvc.perform(patch("/api/members/info/nickname")
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								memberNicknameUpdateDto
						)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(1L))
				.andExpect(jsonPath("$.data.nickname").value("테스트2"))
				.andExpect(jsonPath("$.data.email").value("test@gmail.com"))
				.andDo(print());
	}

	@Test
	@DisplayName("회원 비밀번호 변경 성공")
	@WithMockMember(role = RoleType.USER)
	void successUpdateMemberPassword() throws Exception {
		// given
		MemberPasswordUpdateDto memberPasswordUpdateDto = MemberPasswordUpdateDto.builder()
				.currentPassword("asdfasdfasdf")
				.newPassword("qwerqwerqwer")
				.build();

		given(memberService.updateMemberPassword("test@gmail.com", memberPasswordUpdateDto))
				.willReturn(MemberDetailDto.builder()
						.id(1L)
						.nickname("테스트")
						.email("test@gmail.com")
						.build());

		// when
		// then
		mockMvc.perform(patch("/api/members/info/password")
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								memberPasswordUpdateDto
						)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("회원 비밀번호 변경에 성공하였습니다."))
				.andExpect(jsonPath("$.data.id").value(1L))
				.andExpect(jsonPath("$.data.nickname").value("테스트"))
				.andExpect(jsonPath("$.data.email").value("test@gmail.com"))
				.andDo(print());
	}

	@Test
	@DisplayName("완료된 요리 조회 성공")
	@WithMockMember(role = RoleType.USER)
	void successGetCompletedCooks() throws Exception {
		// given
		CookInfoDto cookInfoDto1 = CookInfoDto.builder()
				.id(1L)
				.recipeInfoDto(RecipeInfoDto.builder()
						.id(1L)
						.title("레시피1")
						.build())
				.build();
		CookInfoDto cookInfoDto2 = CookInfoDto.builder()
				.id(2L)
				.recipeInfoDto(RecipeInfoDto.builder()
						.id(2L)
						.title("레시피2")
						.build())
				.build();

		List<CookInfoDto> list = new ArrayList<>(Arrays.asList(cookInfoDto1, cookInfoDto2));

		given(memberService.getCompletedCooks(anyString(), any()))
				.willReturn(new PageImpl<>(list));

		// when
		// then
		mockMvc.perform(get("/api/members/cooks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[0].id").value(1L))
				.andExpect(jsonPath("$.data.content[0].recipeInfoDto.id").value(1L))
				.andExpect(jsonPath("$.data.content[0].recipeInfoDto.title").value("레시피1"))
				.andExpect(jsonPath("$.data.content[1].id").value(2L))
				.andExpect(jsonPath("$.data.content[1].recipeInfoDto.id").value(2L))
				.andExpect(jsonPath("$.data.content[1].recipeInfoDto.title").value("레시피2"))
				.andDo(print());
	}

	@Test
	@DisplayName("등록한 레시피 내역 조회 성공")
	@WithMockMember(role = RoleType.USER)
	void successGetMyRecipes() throws Exception {
		// given
		RecipeDetailDto recipeDetailDto1 = RecipeDetailDto.builder()
				.id(1L)
				.title("레시피1")
				.summary("레시피1 요약")
				.build();

		RecipeDetailDto recipeDetailDto2 = RecipeDetailDto.builder()
				.id(2L)
				.title("레시피2")
				.summary("레시피2 요약")
				.build();

		List<RecipeDetailDto> list = new ArrayList<>(Arrays.asList(recipeDetailDto1, recipeDetailDto2));

		given(memberService.getMyRecipes(anyString(), any()))
				.willReturn(new PageImpl<>(list));

		// when
		// then
		mockMvc.perform(get("/api/members/recipes"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[0].id").value(1L))
				.andExpect(jsonPath("$.data.content[0].title").value("레시피1"))
				.andExpect(jsonPath("$.data.content[0].summary").value("레시피1 요약"))
				.andExpect(jsonPath("$.data.content[1].id").value(2L))
				.andExpect(jsonPath("$.data.content[1].title").value("레시피2"))
				.andExpect(jsonPath("$.data.content[1].summary").value("레시피2 요약"))
				.andDo(print());
	}

	@Test
	@DisplayName("북마크한 레시피 내역 조회 성공")
	@WithMockMember(role = RoleType.USER)
	void successGetMyBookmarks() throws Exception {
		// given
		RecipeDetailDto recipeDetailDto1 = RecipeDetailDto.builder()
				.id(1L)
				.title("레시피1")
				.summary("레시피1 요약")
				.build();

		RecipeDetailDto recipeDetailDto2 = RecipeDetailDto.builder()
				.id(2L)
				.title("레시피2")
				.summary("레시피2 요약")
				.build();

		List<RecipeDetailDto> list = new ArrayList<>(Arrays.asList(recipeDetailDto1, recipeDetailDto2));
		given(memberService.getMyBookmarks(anyString(), any()))
				.willReturn(new PageImpl<>(list));

		// when
		// then
		mockMvc.perform(get("/api/members/bookmarks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[0].id").value(1L))
				.andExpect(jsonPath("$.data.content[0].title").value("레시피1"))
				.andExpect(jsonPath("$.data.content[0].summary").value("레시피1 요약"))
				.andExpect(jsonPath("$.data.content[1].id").value(2L))
				.andExpect(jsonPath("$.data.content[1].title").value("레시피2"))
				.andExpect(jsonPath("$.data.content[1].summary").value("레시피2 요약"))
				.andDo(print());
	}
}
