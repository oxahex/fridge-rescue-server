package team.rescue.recipe.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static team.rescue.error.type.ServiceError.RECIPE_NOT_FOUND;
import static team.rescue.error.type.ServiceError.USER_NOT_FOUND;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import team.rescue.auth.type.RoleType;
import team.rescue.auth.user.PrincipalDetails;
import team.rescue.common.file.FileService;
import team.rescue.error.exception.ServiceException;
import team.rescue.member.entity.Member;
import team.rescue.member.repository.MemberRepository;
import team.rescue.mock.WithMockMember;
import team.rescue.recipe.dto.RecipeDto.RecipeCreateDto;
import team.rescue.recipe.dto.RecipeDto.RecipeDetailDto;
import team.rescue.recipe.dto.RecipeDto.RecipeInfoDto;
import team.rescue.recipe.dto.RecipeDto.RecipeUpdateDto;
import team.rescue.recipe.dto.RecipeIngredientDto;
import team.rescue.recipe.dto.RecipeStepDto.RecipeStepCreateDto;
import team.rescue.recipe.entity.Recipe;
import team.rescue.recipe.entity.RecipeIngredient;
import team.rescue.recipe.entity.RecipeStep;
import team.rescue.recipe.repository.RecipeIngredientRepository;
import team.rescue.recipe.repository.RecipeRepository;
import team.rescue.recipe.repository.RecipeStepRepository;

@ExtendWith(MockitoExtension.class)
public class RecipeServiceTest {

	@Mock
	MemberRepository memberRepository;

	@Mock
	RecipeRepository recipeRepository;

	@Mock
	RecipeIngredientRepository recipeIngredientRepository;

	@Mock
	RecipeStepRepository recipeStepRepository;

	@Mock
	FileService fileService;

	@InjectMocks
	RecipeService recipeService;

	private List<RecipeIngredientDto> recipeIngredientDtoList;
	private List<RecipeStepCreateDto> recipeStepCreateList;

	@BeforeEach
	public void setup() {
		// Initialize your lists here
		recipeIngredientDtoList = new ArrayList<>();
		// Add items to recipeIngredientDtoList

		recipeStepCreateList = new ArrayList<>();
		// Add items to recipeStepCreateList
	}

	@Test
	@DisplayName("레시피 조회 성공")
	@WithMockMember(role = RoleType.USER)
	void successGetRecipe() {
		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트")
				.email("test@gmail.com")
				.build();

		Recipe recipe = Recipe.builder()
				.id(1L)
				.title("testTitle")
				.summary("testSummary")
				.recipeImageUrl("mockedFilePath")
				.viewCount(0)
				.reportCount(0)
				.reportCount(0)
				.bookmarkCount(0)
				.createdAt(LocalDateTime.now())
				.member(member)
				.build();

		given(recipeRepository.findById(1L)).willReturn(Optional.of(recipe));
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));

		RecipeIngredient recipeIngredient1 = RecipeIngredient.builder()
				.id(1L)
				.recipe(recipe)
				.name("testName1")
				.amount("testAmount1")
				.build();
		RecipeIngredient recipeIngredient2 = RecipeIngredient.builder()
				.id(2L)
				.recipe(recipe)
				.name("testName2")
				.amount("testAmount2")
				.build();
		List<RecipeIngredient> recipeIngredientList = new ArrayList<>(
				Arrays.asList(recipeIngredient1, recipeIngredient2));
		given(recipeIngredientRepository.findByRecipe(recipe)).willReturn(recipeIngredientList);

		RecipeStep recipeStep1 = RecipeStep.builder()
				.id(1L)
				.recipe(recipe)
				.stepNo(1)
				.stepImageUrl("stepImageUrl1")
				.stepDescription("testStepContents1")
				.stepTip("testStepTip1")
				.build();
		RecipeStep recipeStep2 = RecipeStep.builder()
				.id(2L)
				.recipe(recipe)
				.stepNo(2)
				.stepImageUrl("stepImageUrl2")
				.stepDescription("testStepContents2")
				.stepTip("testStepTip2")
				.build();
		List<RecipeStep> recipeStepList = new ArrayList<>(
				Arrays.asList(recipeStep1, recipeStep2));
		given(recipeStepRepository.findByRecipe(recipe)).willReturn(recipeStepList);

		// when
		RecipeDetailDto recipeDetailDto = recipeService.getRecipe(1L);

		// then
		assertEquals(recipeDetailDto.getId(), recipe.getId());
		assertEquals(recipeDetailDto.getTitle(), recipe.getTitle());
		assertEquals(recipeDetailDto.getSummary(), recipe.getSummary());
		assertEquals(recipeDetailDto.getRecipeImageUrl(), recipe.getRecipeImageUrl());
		assertEquals(recipeDetailDto.getViewCount(), recipe.getViewCount());
		assertEquals(recipeDetailDto.getReviewCount(), recipe.getReviewCount());
		assertEquals(recipeDetailDto.getReportCount(), recipe.getReportCount());
		assertEquals(recipeDetailDto.getBookmarkCount(), recipe.getBookmarkCount());
		assertEquals(recipeDetailDto.getCreatedAt(), recipe.getCreatedAt());
	}

	@Test
	@DisplayName("레시피 조회 실패 = 레시피 정보 없음")
	@WithMockMember(role = RoleType.USER)
	void failGetRecipe_RecipeNotFound() {

		// given
		given(recipeRepository.findById(1L)).willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> recipeService.getRecipe(1L));

		// then
		assertEquals(RECIPE_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("레시피 조회 실패 = 사용자 정보 없음")
	@WithMockMember(role = RoleType.USER)
	void failGetRecipe_UserNotFound() {

		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트")
				.email("test@gmail.com")
				.build();

		Recipe recipe = Recipe.builder()
				.id(1L)
				.title("testTitle")
				.summary("testSummary")
				.recipeImageUrl("mockedFilePath")
				.viewCount(0)
				.reportCount(0)
				.reportCount(0)
				.bookmarkCount(0)
				.createdAt(LocalDateTime.now())
				.member(member)
				.build();

		given(recipeRepository.findById(1L)).willReturn(Optional.of(recipe));
		given(memberRepository.findById(1L)).willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> recipeService.getRecipe(1L));

		// then
		assertEquals(RECIPE_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("레시피 등록 성공")
	@WithMockMember(role = RoleType.USER)
	void successAddRecipe() {

		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트")
				.email("test@gmail.com")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		MockMultipartFile mockRecipeImageFile = new MockMultipartFile(
				"file",
				"test.jpg",
				MediaType.IMAGE_JPEG_VALUE,
				"recipe Image test".getBytes()
		);

		given(fileService.uploadImageToS3(mockRecipeImageFile)).willReturn("mockedFilePath");

		Recipe recipe = Recipe.builder()
				.title("testTitle")
				.summary("testSummary")
				.recipeImageUrl("mockedFilePath")
				.viewCount(0)
				.reportCount(0)
				.reportCount(0)
				.bookmarkCount(0)
				.member(member)
				.build();

		RecipeIngredientDto recipeIngredientDto1 = new RecipeIngredientDto();
		recipeIngredientDto1.setName("테스트 재료1");
		recipeIngredientDto1.setAmount("테스트 양1");

		RecipeIngredientDto recipeIngredientDto2 = new RecipeIngredientDto();
		recipeIngredientDto2.setName("테스트 재료2");
		recipeIngredientDto2.setAmount("테스트 양2");

		recipeIngredientDtoList.add(recipeIngredientDto1);
		recipeIngredientDtoList.add(recipeIngredientDto2);

		RecipeStepCreateDto recipeStepDto1 = RecipeStepCreateDto.builder()
				.description("레시피 스탭1")
				.tip("레시피 팁 1")
				.build();

		RecipeStepCreateDto recipeStepDto2 = RecipeStepCreateDto.builder()
				.description("레시피 스탭2")
				.tip("레시피 팁 2")
				.build();

		recipeStepCreateList.add(recipeStepDto1);
		recipeStepCreateList.add(recipeStepDto2);

		// 모의 이미지 파일 생성
		List<MultipartFile> stepImageList = new ArrayList<>();
		MockMultipartFile mockStepImageFile1 = new MockMultipartFile(
				"file",
				"test1.jpg",
				MediaType.IMAGE_JPEG_VALUE,
				"recipe step test 1".getBytes()
		);
		MockMultipartFile mockStepImageFile2 = new MockMultipartFile(
				"file",
				"test2.jpg",
				MediaType.IMAGE_JPEG_VALUE,
				"recipe step test 2".getBytes()
		);
		stepImageList.add(mockStepImageFile1);
		stepImageList.add(mockStepImageFile2);

		RecipeCreateDto recipeCreateDto = RecipeCreateDto.builder()
				.title("testTitle")
				.summary("testSummary")
				.recipeImage(mockRecipeImageFile)
				.ingredients(recipeIngredientDtoList)
				.steps(recipeStepCreateList)
				.stepImages(stepImageList)
				.build();

		// when
		RecipeInfoDto recipeInfoDto =
				recipeService.addRecipe(recipeCreateDto, new PrincipalDetails(member));

		// then
		assertEquals(recipe.getTitle(), recipeInfoDto.getTitle());
		assertEquals(member.getNickname(), recipeInfoDto.getAuthor().getNickname());
	}

	@Test
	@DisplayName("레시피 등록 실패 - 사용자 정보 없음")
	@WithMockMember(role = RoleType.USER)
	void failAddRecipe_UserNotFound() {

		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트")
				.email("test@gmail.com")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> recipeService.addRecipe(any(), new PrincipalDetails(member)));

		// then
		assertEquals(USER_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}


	@Test
	@DisplayName("레시피 업데이트 성공")
	@WithMockMember(role = RoleType.USER)
	void successUpdateRecipe() {

		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트")
				.email("test@gmail.com")
				.build();

		given(memberRepository.findById(1L)).willReturn(Optional.of(member));

		Recipe recipe = Recipe.builder()
				.id(1L)
				.title("testTitle")
				.summary("testSummary")
				.recipeImageUrl("mockedFilePath")
				.viewCount(0)
				.reportCount(0)
				.reportCount(0)
				.bookmarkCount(0)
				.createdAt(LocalDateTime.now())
				.member(member)
				.build();

		given(recipeRepository.findById(1L)).willReturn(Optional.of(recipe));

		RecipeIngredient existingRecipeIngredient1 = RecipeIngredient.builder()
				.name("기존 테스트 재료1")
				.amount("기존 테스트 양1")
				.build();

		RecipeIngredient existingRecipeIngredient2 = RecipeIngredient.builder()
				.name("기존 테스트 재료2")
				.amount("기존 테스트 양2")
				.build();
		List<RecipeIngredient> recipeIngredientList = new ArrayList<>(
				Arrays.asList(existingRecipeIngredient1, existingRecipeIngredient2));
		given(recipeIngredientRepository.findByRecipe(recipe)).willReturn(recipeIngredientList);

		RecipeStep recipeStep1 = RecipeStep.builder()
				.id(1L)
				.stepNo(1)
				.stepImageUrl("mockStepImageUrl1")
				.stepDescription("기존 레시피 스탭1")
				.stepTip("기존 레시피 팁 1")
				.build();
		RecipeStep recipeStep2 = RecipeStep.builder()
				.id(2L)
				.stepNo(2)
				.stepImageUrl("mockStepImageUrl2")
				.stepDescription("기존 레시피 스탭2")
				.stepTip("기존 레시피 팁 2")
				.build();
		List<RecipeStep> recipeStepList = new ArrayList<>(
				Arrays.asList(recipeStep1, recipeStep2));
		given(recipeStepRepository.findByRecipe(recipe)).willReturn(recipeStepList);

		RecipeIngredientDto updatingRecipeIngredient1 = new RecipeIngredientDto();
		updatingRecipeIngredient1.setName("테스트 재료1");
		updatingRecipeIngredient1.setAmount("테스트 양1");

		RecipeIngredientDto updatingRecipeIngredient2 = new RecipeIngredientDto();
		updatingRecipeIngredient2.setName("테스트 재료2");
		updatingRecipeIngredient2.setAmount("테스트 양2");

		List<RecipeIngredientDto> updateRecipeIngredientDtoList = new ArrayList<>(
				Arrays.asList(updatingRecipeIngredient1, updatingRecipeIngredient2));

		// 모의 이미지 파일 생성
		MockMultipartFile mockStepImageFile1 = new MockMultipartFile(
				"file",
				"test1.jpg",
				MediaType.IMAGE_JPEG_VALUE,
				"recipe step test 1".getBytes()
		);
		MockMultipartFile mockStepImageFile2 = new MockMultipartFile(
				"file",
				"test2.jpg",
				MediaType.IMAGE_JPEG_VALUE,
				"recipe step test 2".getBytes()
		);
		RecipeStepCreateDto updatingRecipeStep1 = RecipeStepCreateDto.builder()
				.description("레시피 스탭1")
				.tip("레시피 팁 1")
				.build();
		RecipeStepCreateDto updatingRecipeStep2 = RecipeStepCreateDto.builder()
				.description("레시피 스탭2")
				.tip("레시피 팁 2")
				.build();
		List<RecipeStepCreateDto> updateRecipeStepDtoList = new ArrayList<>(
				Arrays.asList(updatingRecipeStep1, updatingRecipeStep2));

		MockMultipartFile updateMockRecipeImageFile = new MockMultipartFile(
				"file",
				"test.jpg",
				MediaType.IMAGE_JPEG_VALUE,
				"recipe Image test".getBytes()
		);

		// TODO: 이미지 파일 받는 부분 DTO 변경 예정이므로 테스트 수정되어야 함
		RecipeUpdateDto recipeUpdateDto = RecipeUpdateDto.builder()
				.title("업데이트 타이틀")
				.summary("업데이트 서머리")
				.recipeImage(updateMockRecipeImageFile)
				.recipeIngredients(updateRecipeIngredientDtoList)
				.recipeSteps(updateRecipeStepDtoList)
				.build();

		// when
		RecipeDetailDto recipeDetailDto =
				recipeService.updateRecipe(1L, recipeUpdateDto, new PrincipalDetails(member));

		// recipeIngredientRepository.deleteAll 메소드가 올바르게 호출되었는지 확인
		verify(recipeIngredientRepository, times(1)).deleteAll(recipeIngredientList);
		verify(recipeStepRepository, times(1)).deleteAll(recipeStepList);

		assertEquals(recipeDetailDto.getTitle(), recipeUpdateDto.getTitle());
		assertEquals(recipeDetailDto.getSummary(), recipeUpdateDto.getSummary());
//    assertEquals(recipeDetailDto.getRecipeImageUrl(), updateMockRecipeImageFile);
//    assertEquals(recipeDetailDto.getRecipeIngredients(), updateRecipeIngredientDtoList);
//    assertEquals(recipeDetailDto.getRecipeSteps(), updateRecipeStepDtoList);
	}

	@Test
	@DisplayName("레시피 업데이트 실패 - 사용자 정보 없음")
	void failUpdateRecipe_UserNotFound() {

		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트")
				.email("test@gmail.com")
				.build();

		given(memberRepository.findById(1L)).willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> recipeService.updateRecipe(1L, any(), new PrincipalDetails(member)));

		// then
		assertEquals(USER_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("레시피 업데이트 실패 = 레시피 정보 없음")
	@WithMockMember(role = RoleType.USER)
	void failUpdateRecipe_RecipeNotFound() {

		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트")
				.email("test@gmail.com")
				.build();

		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(recipeRepository.findById(1L)).willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> recipeService.updateRecipe(1L, any(), new PrincipalDetails(member)));

		// then
		assertEquals(RECIPE_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("레시피 삭제 성공")
	@WithMockMember(role = RoleType.USER)
	void successDeleteRecipe() {

		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트")
				.email("test@gmail.com")
				.build();

		given(memberRepository.findById(1L))
				.willReturn(Optional.of(member));

		Recipe recipe = Recipe.builder()
				.id(1L)
				.title("testTitle")
				.summary("testSummary")
				.recipeImageUrl("mockedFilePath")
				.viewCount(0)
				.reportCount(0)
				.reportCount(0)
				.bookmarkCount(0)
				.createdAt(LocalDateTime.now())
				.member(member)
				.build();

		given(recipeRepository.findById(1L)).willReturn(Optional.of(recipe));

		RecipeIngredient existingRecipeIngredient1 = RecipeIngredient.builder()
				.name("기존 테스트 재료1")
				.amount("기존 테스트 양1")
				.build();

		RecipeIngredient existingRecipeIngredient2 = RecipeIngredient.builder()
				.name("기존 테스트 재료2")
				.amount("기존 테스트 양2")
				.build();
		List<RecipeIngredient> recipeIngredientList = new ArrayList<>(
				Arrays.asList(existingRecipeIngredient1, existingRecipeIngredient2));
		given(recipeIngredientRepository.findByRecipe(recipe)).willReturn(recipeIngredientList);

		RecipeStep recipeStep1 = RecipeStep.builder()
				.id(1L)
				.stepNo(1)
				.stepImageUrl("mockStepImageUrl1")
				.stepDescription("기존 레시피 스탭1")
				.stepTip("기존 레시피 팁 1")
				.build();
		RecipeStep recipeStep2 = RecipeStep.builder()
				.id(2L)
				.stepNo(2)
				.stepImageUrl("mockStepImageUrl2")
				.stepDescription("기존 레시피 스탭2")
				.stepTip("기존 레시피 팁 2")
				.build();
		List<RecipeStep> recipeStepList = new ArrayList<>(
				Arrays.asList(recipeStep1, recipeStep2));
		given(recipeStepRepository.findByRecipe(recipe)).willReturn(recipeStepList);

		RecipeInfoDto recipeInfoDto =
				recipeService.deleteRecipe(1L, new PrincipalDetails(member));

		verify(recipeIngredientRepository, times(1)).deleteAll(recipeIngredientList);
		verify(recipeStepRepository, times(1)).deleteAll(recipeStepList);
		verify(recipeRepository, times(1)).delete(recipe);

		// then
		assertEquals(recipe.getTitle(), recipeInfoDto.getTitle());
		assertEquals(member.getNickname(), recipeInfoDto.getAuthor().getNickname());
	}

	@Test
	@DisplayName("레시피 삭제 실패 - 사용자 정보 없음 ")
	@WithMockMember(role = RoleType.USER)
	void failDeleteRecipe_UserNotFound() {
		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트")
				.email("test@gmail.com")
				.build();

		given(memberRepository.findById(1L)).willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> recipeService.deleteRecipe(1L, new PrincipalDetails(member)));

		// then
		assertEquals(USER_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("레시피 삭제 실패 = 레시피 정보 없음")
	@WithMockMember(role = RoleType.USER)
	void failDeleteRecipe_RecipeNotFound() {

		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트")
				.email("test@gmail.com")
				.build();

		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(recipeRepository.findById(1L)).willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> recipeService.deleteRecipe(1L, new PrincipalDetails(member)));

		// then
		assertEquals(RECIPE_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}
}
