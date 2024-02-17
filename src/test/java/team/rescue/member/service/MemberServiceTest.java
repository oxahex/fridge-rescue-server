package team.rescue.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static team.rescue.error.type.ServiceError.USER_NOT_FOUND;
import static team.rescue.error.type.ServiceError.USER_PASSWORD_MISMATCH;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import team.rescue.auth.type.RoleType;
import team.rescue.cook.dto.CookDto.CookInfoDto;
import team.rescue.cook.entity.Cook;
import team.rescue.cook.repository.CookRepository;
import team.rescue.error.exception.ServiceException;
import team.rescue.member.dto.MemberDto.MemberDetailDto;
import team.rescue.member.dto.MemberDto.MemberNicknameUpdateDto;
import team.rescue.member.dto.MemberDto.MemberPasswordUpdateDto;
import team.rescue.member.entity.Member;
import team.rescue.member.repository.MemberRepository;
import team.rescue.mock.WithMockMember;
import team.rescue.recipe.dto.RecipeDto.RecipeDetailDto;
import team.rescue.recipe.entity.Bookmark;
import team.rescue.recipe.entity.Recipe;
import team.rescue.recipe.repository.BookmarkRepository;
import team.rescue.recipe.repository.RecipeRepository;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	@Mock
	MemberRepository memberRepository;

	@Mock
	CookRepository cookRepository;

	@Mock
	RecipeRepository recipeRepository;

	@Mock
	BookmarkRepository bookmarkRepository;

	@Spy
	PasswordEncoder passwordEncoder;

	@InjectMocks
	MemberService memberService;

	@Test
	@DisplayName("회원 정보 조회 성공")
	void successGetMemberInfo() {
		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트")
				.email("test@gmail.com")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		// when
		MemberDetailDto memberDetailDto = memberService.getMemberInfo("test@gmail.com");

		// then
		assertEquals("테스트", memberDetailDto.getNickname());
		assertEquals("test@gmail.com", memberDetailDto.getEmail());
	}

	@Test
	@DisplayName("회원 정보 조회 실패 - 회원 정보 없음")
	void failGetMemberInfo_UserNotFound() {
		// given
		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> memberService.getMemberInfo("test@gmail.com"));

		// then
		assertEquals(USER_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("회원 닉네임 변경 성공")
	void successUpdateMemberNickname() {
		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트")
				.email("test@gmail.com")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		member.updateNickname("테스트2");

		given(memberRepository.save(member))
				.willReturn(Member.builder()
						.id(1L)
						.nickname("테스트2")
						.email("test@gmail.com")
						.build());

		// when
		MemberDetailDto memberDetailDto = memberService.updateMemberNickname("test@gmail.com",
				MemberNicknameUpdateDto.builder()
						.nickname("테스트2")
						.build());

		// then
		assertEquals("테스트2", memberDetailDto.getNickname());
		assertEquals("test@gmail.com", memberDetailDto.getEmail());
	}

	@Test
	@DisplayName("회원 닉네임 변경 실패 - 사용자 정보 없음")
	void failUpdateMemberNickname_UserNotFound() {
		// given
		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> memberService.getMemberInfo("test@gmail.com"));

		// then
		assertEquals(USER_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("회원 비밀번호 변경 성공")
	void successUpdateMemberPassword() {
		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트")
				.email("test@gmail.com")
				.password("1234567890")
				.build();

		MemberPasswordUpdateDto memberPasswordUpdateDto = MemberPasswordUpdateDto.builder()
				.currentPassword("1234567890")
				.newPassword("0987654321")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		given(passwordEncoder.matches(memberPasswordUpdateDto.getCurrentPassword(),
				member.getPassword()))
				.willReturn(true);

		given(memberRepository.save(any()))
				.willReturn(Member.builder()
						.id(1L)
						.nickname("테스트")
						.email("test@gmail.com")
						.password("0987654321")
						.build());

		// when
		MemberDetailDto memberDetailDto = memberService.updateMemberPassword("test@gmail.com",
				memberPasswordUpdateDto);

		// then
		assertEquals("테스트", memberDetailDto.getNickname());
		assertEquals("test@gmail.com", memberDetailDto.getEmail());
	}

	@Test
	@DisplayName("회원 비밀번호 변경 실패 - 사용자 정보 없음")
	void failUpdateMemberPassword_UserNotFound() {
		// given
		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> memberService.getMemberInfo("test@gmail.com"));

		// then
		assertEquals(USER_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("회원 비밀번호 변경 실패 - 현재 비밀번호 불일치")
	void failUpdateMemberPassword_UserPasswordMismatch() {
		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트")
				.email("test@gmail.com")
				.password("1234567890")
				.build();

		MemberPasswordUpdateDto memberPasswordUpdateDto = MemberPasswordUpdateDto.builder()
				.currentPassword("1234567890")
				.newPassword("0987654321")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		given(passwordEncoder.matches(memberPasswordUpdateDto.getCurrentPassword(),
				member.getPassword()))
				.willReturn(false);

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> memberService.updateMemberPassword("test@gmail.com", memberPasswordUpdateDto));

		// then
		assertEquals(USER_PASSWORD_MISMATCH.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("완성된 요리 조회 성공")
	@WithMockMember(role = RoleType.USER)
	void successGetCompletedCooks() {
		// given
		Member member = Member.builder()
				.id(1L)
				.email("test@gmail.com")
				.build();

		Member m = Member.builder()
				.id(2L)
				.email("author@gmail.com")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		Recipe recipe1 = Recipe.builder()
				.member(m)
				.title("레시피1")
				.build();
		Recipe recipe2 = Recipe.builder()
				.member(m)
				.title("레시피2")
				.build();

		Cook cook1 = Cook.builder()
				.id(1L)
				.member(member)
				.recipe(recipe1)
				.build();

		Cook cook2 = Cook.builder()
				.id(2L)
				.member(member)
				.recipe(recipe2)
				.build();

		List<Cook> list = new ArrayList<>(Arrays.asList(cook1, cook2));

		given(cookRepository.findByMember(any(Member.class), any(Pageable.class)))
				.willReturn(new PageImpl<>(list));

		// when
		Page<CookInfoDto> cookInfoDtoPage = memberService.getCompletedCooks("test@gmail.com",
				PageRequest.of(0, 2));

		// then
		assertEquals(1L, cookInfoDtoPage.getContent().get(0).getId());
		assertEquals("레시피1", cookInfoDtoPage.getContent().get(0).getRecipeInfoDto().getTitle());
		assertEquals(2L, cookInfoDtoPage.getContent().get(1).getId());
		assertEquals("레시피2", cookInfoDtoPage.getContent().get(1).getRecipeInfoDto().getTitle());
	}

	@Test
	@DisplayName("완료된 요리 조회 실패 - 사용자 정보 없음")
	@WithMockMember(role = RoleType.USER)
	void failGetCompletedCooks_UserNotFound() {
		// given
		given(memberRepository.findUserByEmail(anyString()))
				.willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> memberService.getCompletedCooks("test@gmail.com", PageRequest.of(0, 2)));

		// then
		assertEquals(USER_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("등록한 레시피 내역 조회 성공")
	@WithMockMember(role = RoleType.USER)
	void successGetMyRecipes() {
		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트1")
				.email("test@gmail.com")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		Recipe recipe1 = Recipe.builder()
				.id(1L)
				.member(member)
				.title("레시피1")
				.summary("레시피1 요약")
				.build();

		Recipe recipe2 = Recipe.builder()
				.id(2L)
				.member(member)
				.title("레시피2")
				.summary("레시피2 요약")
				.build();

		List<Recipe> list = new ArrayList<>(Arrays.asList(recipe1, recipe2));

		given(recipeRepository.findByMember(member, PageRequest.of(0, 2)))
				.willReturn(new PageImpl<>(list));

		// when
		Page<RecipeDetailDto> myRecipes = memberService.getMyRecipes("test@gmail.com",
				PageRequest.of(0, 2));

		// then
		assertEquals(1L, myRecipes.getContent().get(0).getId());
		assertEquals("레시피1", myRecipes.getContent().get(0).getTitle());
		assertEquals("레시피1 요약", myRecipes.getContent().get(0).getSummary());
		assertEquals(2L, myRecipes.getContent().get(1).getId());
		assertEquals("레시피2", myRecipes.getContent().get(1).getTitle());
		assertEquals("레시피2 요약", myRecipes.getContent().get(1).getSummary());
	}

	@Test
	@DisplayName("등록한 레시피 내역 조회 실패 - 사용자 정보 없음")
	@WithMockMember(role = RoleType.USER)
	void failGetMyRecipes_UserNotFound() {
		// given
		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> memberService.getMyRecipes("test@gmail.com", PageRequest.of(0, 2)));

		// then
		assertEquals(USER_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("북마크한 레시피 내역 조회 성공")
	@WithMockMember(role = RoleType.USER)
	void successGetMyBookmarks() {
		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트1")
				.email("test@gmail.com")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		Recipe recipe1 = Recipe.builder()
				.id(1L)
				.member(member)
				.title("레시피1")
				.summary("레시피1 요약")
				.build();

		Recipe recipe2 = Recipe.builder()
				.id(2L)
				.member(member)
				.title("레시피2")
				.summary("레시피2 요약")
				.build();

		Bookmark bookmark1 = Bookmark.builder()
				.member(member)
				.id(1L)
				.recipe(recipe1)
				.build();

		Bookmark bookmark2 = Bookmark.builder()
				.member(member)
				.id(1L)
				.recipe(recipe2)
				.build();

		List<Bookmark> list = new ArrayList<>(Arrays.asList(bookmark1, bookmark2));

		given(bookmarkRepository.findByMember(member, PageRequest.of(0, 2)))
				.willReturn(new PageImpl<>(list));

		// when
		Page<RecipeDetailDto> myBookmarks = memberService.getMyBookmarks("test@gmail.com",
				PageRequest.of(0, 2));

		// then
		assertEquals(1L, myBookmarks.getContent().get(0).getId());
		assertEquals("레시피1", myBookmarks.getContent().get(0).getTitle());
		assertEquals("레시피1 요약", myBookmarks.getContent().get(0).getSummary());
		assertEquals(2L, myBookmarks.getContent().get(1).getId());
		assertEquals("레시피2", myBookmarks.getContent().get(1).getTitle());
		assertEquals("레시피2 요약", myBookmarks.getContent().get(1).getSummary());
	}

	@Test
	@DisplayName("북마크한 레시피 내역 조회 실패 - 사용자 정보 없음")
	@WithMockMember(role = RoleType.USER)
	void failGetMyBookmarks_UserNotFound() {
		// given
		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> memberService.getMyBookmarks("test@gmail.com", PageRequest.of(0, 2)));

		// then
		assertEquals(USER_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}
}
