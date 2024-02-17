package team.rescue.recipe.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import team.rescue.auth.type.ProviderType;
import team.rescue.auth.type.RoleType;
import team.rescue.member.dto.MemberDto.MemberInfoDto;
import team.rescue.member.entity.Member;
import team.rescue.member.repository.MemberRepository;
import team.rescue.mock.MockMember;
import team.rescue.mock.WithMockMember;
import team.rescue.recipe.dto.BookmarkDto.BookmarkInfoDto;
import team.rescue.recipe.dto.RecipeDto.RecipeCreateDto;
import team.rescue.recipe.dto.RecipeDto.RecipeDetailDto;
import team.rescue.recipe.dto.RecipeIngredientDto;
import team.rescue.recipe.dto.RecipeStepDto.RecipeStepCreateDto;
import team.rescue.recipe.dto.RecipeStepDto.RecipeStepInfoDto;
import team.rescue.recipe.service.RecipeService;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@ActiveProfiles(profiles = "test")
@Transactional
class RecipeControllerTest extends MockMember {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	private MemberRepository memberRepository;

	@MockBean
	RecipeService recipeService;

	private Member existMember;

	@BeforeEach
	public void setMember() {
		this.existMember = memberRepository.save(
				getNewMember("test", ProviderType.EMAIL, RoleType.USER)
		);
	}

	List<RecipeIngredientDto> recipeIngredientList = new ArrayList<>();

	@BeforeEach
	public void setRecipeIngredientInfoDtoList() {
		RecipeIngredientDto recipeIngredientDto1 = new RecipeIngredientDto();
		recipeIngredientDto1.setName("마늘");
		recipeIngredientDto1.setAmount("3쫑");

		RecipeIngredientDto recipeIngredientDto2 = new RecipeIngredientDto();
		recipeIngredientDto1.setName("양파");
		recipeIngredientDto1.setAmount("2개");

		RecipeIngredientDto recipeIngredientDto3 = new RecipeIngredientDto();
		recipeIngredientDto1.setName("쪽파");
		recipeIngredientDto1.setAmount("2단");

		recipeIngredientList.add(recipeIngredientDto1);
		recipeIngredientList.add(recipeIngredientDto2);
		recipeIngredientList.add(recipeIngredientDto3);

	}

	List<RecipeStepInfoDto> recipeStepInfoList = new ArrayList<>();

	@BeforeEach
	public void setRecipeStepInfoDtoList() {
		RecipeStepInfoDto recipeStepInfoDto1 = RecipeStepInfoDto.builder()
				.stepNo(1)
				.stepImageUrl("http://testRecipeSTEP1.com/image.jpg")
				.stepDescription("레시피 스탭1")
				.stepTip("레시피 팁 1")
				.build();
		RecipeStepInfoDto recipeStepInfoDto2 = RecipeStepInfoDto.builder()
				.stepNo(1)
				.stepImageUrl("http://testRecipeSTEP2.com/image.jpg")
				.stepDescription("레시피 스탭2")
				.stepTip("레시피 팁 2")
				.build();

		recipeStepInfoList.add(recipeStepInfoDto1);
		recipeStepInfoList.add(recipeStepInfoDto2);
	}


	List<RecipeStepCreateDto> recipeStepCreateList = new ArrayList<>();

	@BeforeEach
	public void setRecipeStepCreateDtoList() {
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

	}

	List<MultipartFile> stepImageList = new ArrayList<>();

	@BeforeEach
	public void setStepImageDtoList() {
		// 모의 이미지 파일 생성
		MockMultipartFile mockImageFile1 = new MockMultipartFile(
				"file",
				"test1.jpg",
				MediaType.IMAGE_JPEG_VALUE,
				"recipe step test 1".getBytes()
		);
		MockMultipartFile mockImageFile2 = new MockMultipartFile(
				"file",
				"test2.jpg",
				MediaType.IMAGE_JPEG_VALUE,
				"recipe step test 2".getBytes()
		);

		stepImageList.add(mockImageFile1);
		stepImageList.add(mockImageFile2);
	}

	@Test
	@DisplayName("레시피 조회 성공")
	@WithMockMember(role = RoleType.USER)
	void successGetRecipe() throws Exception {
		// given
		Long recipeId = 1L;

		MemberInfoDto mockAuthor = MemberInfoDto.of(existMember);

		given(recipeService.getRecipe(anyLong()))
				.willReturn(RecipeDetailDto.builder()
						.id(recipeId)
						.title("레시피 타이틀 테스트")
						.summary("레시피 요약 테스트")
						.recipeImageUrl("http://testRecipe.com/image.jpg")
						.viewCount(100)
						.reviewCount(20)
						.reportCount(0)
						.bookmarkCount(10)
						.createdAt(LocalDateTime.now())
						.recipeIngredients(recipeIngredientList)
						.recipeSteps(recipeStepInfoList)
						.author(mockAuthor)
						.build());

		// when
		// then
		mockMvc.perform(get("/api/recipes/" + recipeId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(recipeId))
				.andExpect(jsonPath("$.data.title").value("레시피 타이틀 테스트"))
				.andExpect(jsonPath("$.data.summary").value("레시피 요약 테스트"))
				.andExpect(jsonPath("$.data.recipeImageUrl").value("http://testRecipe.com/image.jpg"))
				.andExpect(jsonPath("$.data.viewCount").value(100))
				.andExpect(jsonPath("$.data.reviewCount").value(20))
				.andExpect(jsonPath("$.data.reportCount").value(0))
				.andExpect(jsonPath("$.data.bookmarkCount").value(10))
				.andDo(print());
	}

	@Test
	@DisplayName("레시피 등록 성공")
	@WithMockMember(role = RoleType.USER)
	void successRecipe() throws Exception {
// given
		MockMultipartFile mockRecipeImageFile = new MockMultipartFile(
				"file",
				"test.jpg",
				MediaType.IMAGE_JPEG_VALUE,
				"recipe Image test".getBytes()
		);

		RecipeCreateDto recipeCreateDto = RecipeCreateDto.builder()
				.title("새로운 레시피")
				.summary("레시피 요약")
				.recipeImage(mockRecipeImageFile)
				.ingredients(recipeIngredientList)
				.steps(recipeStepCreateList)
				.stepImages(stepImageList)
				.build();

//    given(recipeService.addRecipe(any(RecipeCreateDto.class), any(PrincipalDetails.class)))
//        .willReturn(recipeCreateDto);

		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		// when
		// then
		mockMvc.perform(
						post("/api/recipes")
								.contentType(MediaType.MULTIPART_FORM_DATA)
								.content(objectMapper.writeValueAsString(
										recipeCreateDto
								)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.title").value("새로운 레시피"))
				.andExpect(jsonPath("$.data.summary").value("레시피 요약"))
				.andExpect(jsonPath("$.data.recipeIngredients[0].name").value("마늘"))
				.andExpect(jsonPath("$.data.recipeIngredients[0].amount").value("3쫑"))
				.andExpect(jsonPath("$.data.recipeIngredients[1].name").value("양파"))
				.andExpect(jsonPath("$.data.recipeIngredients[1].amount").value("2개"))
				.andExpect(jsonPath("$.data.recipeIngredients[2].name").value("쪽파"))
				.andExpect(jsonPath("$.data.recipeIngredients[2].amount").value("2단"))
				.andExpect(jsonPath("$.data.recipeSteps[0].stepContents").value("레시피 스탭1"))
				.andExpect(jsonPath("$.data.recipeSteps[0].stepTip").value("레시피 팁 1"))
				.andExpect(jsonPath("$.data.recipeSteps[1].stepContents").value("레시피 스탭2"))
				.andExpect(jsonPath("$.data.recipeSteps[1].stepTip").value("레시피 팁 2"))
				.andDo(print());
	}

	@Test
	@DisplayName("북마크 등록 성공")
	@WithMockMember(role = RoleType.USER)
	void successBookmarkRecipe() throws Exception {
		// given
		given(recipeService.bookmarkRecipe(anyLong(), anyString()))
				.willReturn(BookmarkInfoDto.builder()
						.bookmarkCount(1)
						.isBookmarked(true)
						.build());

		// when
		// then
		mockMvc.perform(post("/api/recipes/1/bookmark"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.bookmarkCount").value(1))
				.andExpect(jsonPath("$.data.isBookmarked").value(true))
				.andDo(print());
	}

	@Test
	@DisplayName("북마크 취소 성공")
	@WithMockMember(role = RoleType.USER)
	void successCancelBookmarkRecipe() throws Exception {
		// given
		given(recipeService.bookmarkRecipe(anyLong(), anyString()))
				.willReturn(BookmarkInfoDto.builder()
						.bookmarkCount(0)
						.isBookmarked(false)
						.build());

		// when
		// then
		mockMvc.perform(post("/api/recipes/1/bookmark"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.bookmarkCount").value(0))
				.andExpect(jsonPath("$.data.isBookmarked").value(false))
				.andDo(print());
	}

}
