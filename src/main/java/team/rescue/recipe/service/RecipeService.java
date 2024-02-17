package team.rescue.recipe.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import team.rescue.aop.DistributedLock;
import team.rescue.auth.user.PrincipalDetails;
import team.rescue.common.file.FileService;
import team.rescue.error.exception.ServiceException;
import team.rescue.error.type.ServiceError;
import team.rescue.member.dto.MemberDto.MemberInfoDto;
import team.rescue.member.entity.Member;
import team.rescue.member.repository.MemberRepository;
import team.rescue.recipe.dto.BookmarkDto.BookmarkInfoDto;
import team.rescue.recipe.dto.RecipeDto.RecipeCreateDto;
import team.rescue.recipe.dto.RecipeDto.RecipeDetailDto;
import team.rescue.recipe.dto.RecipeDto.RecipeInfoDto;
import team.rescue.recipe.dto.RecipeDto.RecipeUpdateDto;
import team.rescue.recipe.dto.RecipeIngredientDto.RecipeIngredientCreateDto;
import team.rescue.recipe.dto.RecipeIngredientDto.RecipeIngredientInfoDto;
import team.rescue.recipe.dto.RecipeStepDto.RecipeStepCreateDto;
import team.rescue.recipe.dto.RecipeStepDto.RecipeStepDeleteDto;
import team.rescue.recipe.dto.RecipeStepDto.RecipeStepInfoDto;
import team.rescue.recipe.dto.RecipeStepDto.RecipeStepUpdateDto;
import team.rescue.recipe.entity.Bookmark;
import team.rescue.recipe.entity.Recipe;
import team.rescue.recipe.entity.RecipeIngredient;
import team.rescue.recipe.entity.RecipeStep;
import team.rescue.recipe.repository.BookmarkRepository;
import team.rescue.recipe.repository.RecipeIngredientRepository;
import team.rescue.recipe.repository.RecipeRepository;
import team.rescue.recipe.repository.RecipeStepRepository;
import team.rescue.search.entity.RecipeDoc;
import team.rescue.search.repository.RecipeSearchRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

	private final FileService fileService;
	private final RecipeRepository recipeRepository;
	private final RecipeIngredientRepository recipeIngredientRepository;
	private final RecipeStepRepository recipeStepRepository;
	private final RecipeSearchRepository recipeSearchRepository;
	private final MemberRepository memberRepository;
	private final BookmarkRepository bookmarkRepository;

	/**
	 * 현재 시각 기준으로 전체 레시피 개수 반환
	 *
	 * @return 전체 레시피 개수
	 */
	public Integer getTotalRecipeCount() {
		return recipeRepository.countByCreatedAtBefore(LocalDateTime.now());
	}

	@Transactional
	@DistributedLock(prefix = "get_recipe")
	public RecipeDetailDto getRecipe(Long id, PrincipalDetails details) {

		Recipe recipe = recipeRepository.findById(id)
				.orElseThrow(() -> {
					log.error("레시피 없음");
					return new ServiceException(ServiceError.RECIPE_NOT_FOUND);
				});
		log.debug("레시피 {}", recipe);

		Long memberId = recipe.getMember().getId();
		log.debug("member Id {}", memberId);

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> {
					log.error("일치하는 사용자 정보 없음");
					return new ServiceException(ServiceError.USER_NOT_FOUND);
				});

		MemberInfoDto memberInfoDto = MemberInfoDto.of(member);

		List<RecipeIngredient> recipeIngredientList =
				recipeIngredientRepository.findByRecipe(recipe);
		List<RecipeIngredientInfoDto> recipeIngredientDtoList =
				recipeIngredientList.stream().map(RecipeIngredientInfoDto::of).toList();

		List<RecipeStep> recipeStepList =
				recipeStepRepository.findByRecipe(recipe);
		List<RecipeStepInfoDto> recipeStepDtoList =
				recipeStepList.stream().map(RecipeStepInfoDto::of).toList();

		recipe.increaseViewCount();

		// 레시피 북마크 여부 반환
		boolean isBookmarked = false;
		if (details != null) {
			isBookmarked = bookmarkRepository.existsByRecipeAndMember(recipe, details.getMember());
		}

		return RecipeDetailDto.builder()
				.id(recipe.getId())
				.title(recipe.getTitle())
				.summary(recipe.getSummary())
				.recipeImageUrl(recipe.getRecipeImageUrl())
				.viewCount(recipe.getViewCount())
				.reviewCount(recipe.getReviewCount())
				.reportCount(recipe.getReportCount())
				.bookmarkCount(recipe.getBookmarkCount())
				.createdAt(recipe.getCreatedAt())
				.recipeIngredients(recipeIngredientDtoList)
				.recipeSteps(recipeStepDtoList)
				.author(memberInfoDto)
				.isBookmarked(isBookmarked)
				.build();
	}

	@Transactional
	public RecipeInfoDto addRecipe(
			RecipeCreateDto info,
			MultipartFile recipeImage,
			List<MultipartFile> stepImages,
			PrincipalDetails principalDetails
	) {

		String memberEmail = principalDetails.getMember().getEmail();
		log.info("[레시피 생성] userEmail={}", memberEmail);

		Member member = memberRepository.findUserByEmail(memberEmail)
				.orElseThrow(() -> {
					log.error("일치하는 사용자 정보 없음");
					return new ServiceException(ServiceError.USER_NOT_FOUND);
				});

		// 레시피 대표 이미지 저장
		String recipeImageFilePath = fileService.uploadImageToS3(recipeImage);

		Recipe recipe = Recipe.builder()
				.title(info.getTitle())
				.summary(info.getSummary())
				.recipeImageUrl(recipeImageFilePath)
				.viewCount(0)
				.reviewCount(0)
				.reportCount(0)
				.bookmarkCount(0)
				.isBlocked(false)
				.member(member) // 멤버 연결
				.build();

		recipeRepository.save(recipe); // 먼저 Recipe 저장

		// 재료 저장
		List<RecipeIngredient> ingredients = new ArrayList<>();
		for (RecipeIngredientCreateDto ingredientDto : info.getIngredients()) {
			RecipeIngredient ingredient = RecipeIngredient.builder()
					.name(ingredientDto.getName())
					.amount(ingredientDto.getAmount())
					.recipe(recipe) // 재료와 레시피 연결
					.build();
			ingredients.add(ingredient);
		}
		recipeIngredientRepository.saveAll(ingredients);

		// Recipe Document 저장
		RecipeDoc recipeDoc = RecipeDoc.of(recipe, ingredients, member, recipeImageFilePath);
		recipeSearchRepository.save(recipeDoc);

		// 레시피 스탭들 저장
		for (int i = 0; i < info.getSteps().size(); i++) {

			RecipeStepCreateDto stepDto = info.getSteps().get(i);
			MultipartFile imageFile = stepImages.get(i);

			// 이미지 파일이 존재하면 저장
			String stepImageUrl = null;
			if (imageFile.getSize() > 0) {
				stepImageUrl = fileService.uploadImageToS3(imageFile);
			}

			RecipeStep step = RecipeStep.builder()
					.stepNo(i)
					.stepImageUrl(stepImageUrl) // URL 설정
					.stepDescription(stepDto.getDescription())
					.stepTip(stepDto.getTip())
					.recipe(recipe) // 레시피와 연결
					.build();

			recipeStepRepository.save(step);
		}

		return RecipeInfoDto.of(recipe);
	}


	@Transactional
	public RecipeInfoDto updateRecipe(
			Long recipeId,
			RecipeUpdateDto info,
			MultipartFile recipeImage,
			List<MultipartFile> stepImages,
			PrincipalDetails principalDetails
	) {

		Long memberId = principalDetails.getMember().getId();

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> {
					log.error("일치하는 사용자 정보 없음");
					return new ServiceException(ServiceError.USER_NOT_FOUND);
				});

		Recipe recipe = recipeRepository.findById(recipeId)
				.orElseThrow(() -> {
					log.error("레시피 없음");
					return new ServiceException(ServiceError.RECIPE_NOT_FOUND);
				});

		if (!recipe.getMember().equals(member)) {
			log.error("레시피를 작성한 회원이 아님");
			throw new ServiceException(ServiceError.RECIPE_MEMBER_UNMATCHED);
		}

		// 레시피 재료 수정 작업
		// 기존 레시피 ingredient 삭제
		List<RecipeIngredient> existingRecipeIngredientList =
				recipeIngredientRepository.findByRecipe(recipe);
		recipeIngredientRepository.deleteAll(existingRecipeIngredientList);

		// 레시피 ingredient 추가
		List<RecipeIngredient> ingredients = new ArrayList<>();
		for (RecipeIngredientCreateDto ingredientDto : info.getIngredients()) {
			RecipeIngredient ingredient = RecipeIngredient.builder()
					.name(ingredientDto.getName())
					.amount(ingredientDto.getAmount())
					.recipe(recipe) // 재료와 레시피 연결
					.build();
			ingredients.add(ingredient);
		}
		recipeIngredientRepository.saveAll(ingredients);

		List<RecipeIngredientInfoDto> updatedRecipeIngredients = new ArrayList<>();
		for (RecipeIngredient recipeIngredient : ingredients) {
			updatedRecipeIngredients.add(RecipeIngredientInfoDto.of(recipeIngredient));
		}

		// 레시피 스탭 수정 작업
		// 기존 레시피 스탭 삭제
		for (RecipeStepDeleteDto existingRecipeStep : info.getDeleteSteps()) {
			// 이미지가 있는 스텝이면 s3에서 삭제
			RecipeStep recipeStep = recipeStepRepository.findById(existingRecipeStep.getId())
					.orElseThrow(() -> {
						log.error("스탭을 불러올 수 없습니다.");
						return new ServiceException(ServiceError.RECIPE_STEP_NOT_FOUND);
					});
			if (!(recipeStep.getStepImageUrl() == null || recipeStep.getStepImageUrl().isEmpty())) {
				fileService.deleteImages(recipeStep.getStepImageUrl());
			}
			// 스탭 데이터 삭제
			recipeStepRepository.delete(recipeStep);
		}

		// 레시피 스탭 추가 / 수정
		List<RecipeStepInfoDto> updatedRecipeStep = new ArrayList<>();
		for (int i = 0; i < info.getUpdateSteps().size(); i++) {
			RecipeStepUpdateDto recipeStepUpdateDto = info.getUpdateSteps().get(i);
			RecipeStep recipeStep = null;

			// 스탭 id가 null이면 새로운 스탭 추가
			if (recipeStepUpdateDto.getId() == null) {
				// 추가할 이미지가 있는 스탭이라면 이미지 저장
				recipeStep = createNewRecipeStep(recipeStepUpdateDto, stepImages.get(i), recipe);

			}
			// 스탭 id가 있다면, 기존 스탭 수정
			else {
				recipeStep = updateExistingRecipeStep(recipeStepUpdateDto, stepImages.get(i));
			}
			recipeStepRepository.save(recipeStep);
			updatedRecipeStep.add(RecipeStepInfoDto.of(recipeStep));
		}

		// 대표 이미지 수정
		if (recipeImage != null && !recipeImage.isEmpty() &&
				recipeImage.getSize() > 0 && !Objects.equals(recipeImage.getContentType(), "String")) {
			String recipeImageFilePath = recipe.getRecipeImageUrl();
			// 레시피 대표 이미지 업데이트
			fileService.deleteImages(recipeImageFilePath);
			recipeImageFilePath = fileService.uploadImageToS3(recipeImage);

			recipe.update(
					info.getTitle(),
					info.getSummary(),
					recipeImageFilePath
			);
		} else {
			recipe.updateWithoutImage(
					info.getTitle(),
					info.getSummary()
			);
		}


		recipeRepository.save(recipe);

		return RecipeInfoDto.of(recipe);
	}

	@Transactional
	public RecipeInfoDto deleteRecipe(
			Long recipeId,
			PrincipalDetails principalDetails
	) {

		Long memberId = principalDetails.getMember().getId();

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> {
					log.error("일치하는 사용자 정보 없음");
					return new ServiceException(ServiceError.USER_NOT_FOUND);
				});

		Recipe recipe = recipeRepository.findById(recipeId)
				.orElseThrow(() -> {
					log.error("레시피 없음");
					return new ServiceException(ServiceError.RECIPE_NOT_FOUND);
				});

		if (!recipe.getMember().equals(member)) {
			log.error("레시피를 작성한 회원이 아님");
			throw new ServiceException(ServiceError.RECIPE_MEMBER_UNMATCHED);
		}

		// 레시피 대표 이미지 삭제
		fileService.deleteImages(recipe.getRecipeImageUrl());

		// 레시피 ingredient 삭제
		List<RecipeIngredient> existingRecipeIngredientList =
				recipeIngredientRepository.findByRecipe(recipe);
		recipeIngredientRepository.deleteAll(existingRecipeIngredientList);

		// 레시피 step 삭제
		List<RecipeStep> existingRecipeStepList = recipeStepRepository.findByRecipe(recipe);
		for (RecipeStep existingRecipeStep : existingRecipeStepList) {

			// 이미지가 있는 스텝이면 s3에서 삭제
			if (!(existingRecipeStep.getStepImageUrl() == null || existingRecipeStep.getStepImageUrl()
					.isEmpty())) {
				fileService.deleteImages(existingRecipeStep.getStepImageUrl());
			}
		}
		recipeStepRepository.deleteAll(existingRecipeStepList);

		recipeRepository.delete(recipe);

		return RecipeInfoDto.of(recipe);
	}

	@Transactional
	@DistributedLock(prefix = "bookmark_recipe")
	public BookmarkInfoDto bookmarkRecipe(Long recipeId, String email) {
		Member member = memberRepository.findUserByEmail(email)
				.orElseThrow(() -> new ServiceException(ServiceError.USER_NOT_FOUND));

		Recipe recipe = recipeRepository.findById(recipeId)
				.orElseThrow(() -> new ServiceException(ServiceError.RECIPE_NOT_FOUND));

		Optional<Bookmark> bookmarkOptional = bookmarkRepository.findByRecipeAndMember(recipe, member);

		// 이미 사용자가 해당 레시피를 북마크 한 경우
		if (bookmarkOptional.isPresent()) {
			bookmarkRepository.deleteByRecipeAndMember(recipe, member);
			recipe.decreaseBookmarkCount();
			recipeRepository.save(recipe);

			return BookmarkInfoDto.builder()
					.bookmarkCount(recipe.getBookmarkCount())
					.isBookmarked(false)
					.build();
		} else { // 사용자가 해당 레시피를 북마크 하지 않은 경우
			Bookmark bookmark = Bookmark.builder()
					.recipe(recipe)
					.member(member)
					.build();

			bookmarkRepository.save(bookmark);
			recipe.increaseBookmarkCount();
			recipeRepository.save(recipe);

			return BookmarkInfoDto.builder()
					.bookmarkCount(recipe.getBookmarkCount())
					.isBookmarked(true)
					.build();
		}
	}

	@Transactional(readOnly = true)
	public Page<RecipeInfoDto> getRecentRecipes(Pageable pageable) {
		Page<Recipe> recipePage = recipeRepository.findAllByOrderByCreatedAtDesc(pageable);

		return recipePage.map(RecipeInfoDto::of);
	}

	@Transactional(readOnly = true)
	public Page<RecipeInfoDto> getPopularRecipes(Pageable pageable) {
		Page<Recipe> recipePage = recipeRepository.findAllByOrderByBookmarkCountDesc(pageable);

		return recipePage.map(RecipeInfoDto::of);
	}

	private RecipeStep createNewRecipeStep(RecipeStepUpdateDto dto, MultipartFile imageFile,
			Recipe recipe) {
		String stepImageUrl = null;
		if (imageFile.getSize() > 0) {
			stepImageUrl = fileService.uploadImageToS3(imageFile);
		}

		return RecipeStep.builder()
				.stepNo(dto.getStepNo())
				.stepImageUrl(stepImageUrl)
				.stepDescription(dto.getDescription())
				.stepTip(dto.getTip())
				.recipe(recipe)
				.build();
	}

	private RecipeStep updateExistingRecipeStep(RecipeStepUpdateDto dto, MultipartFile imageFile) {
		RecipeStep recipeStep = recipeStepRepository.findById(dto.getId())
				.orElseThrow(() -> {
					log.error("스탭을 불러올 수 없습니다.");
					return new ServiceException(ServiceError.RECIPE_STEP_NOT_FOUND);
				});

		if (imageFile.getSize() > 0 && !Objects.equals(imageFile.getContentType(), "String")) {
			String stepImageUrl = fileService.uploadImageToS3(imageFile);
			fileService.deleteImages(recipeStep.getStepImageUrl());
			recipeStep.updateRecipeStep(dto.getStepNo(), stepImageUrl, dto.getDescription(),
					dto.getTip());
		} else {
			recipeStep.updateRecipeStepWithoutImage(dto.getStepNo(), dto.getDescription(), dto.getTip());
		}

		return recipeStep;
	}
}
