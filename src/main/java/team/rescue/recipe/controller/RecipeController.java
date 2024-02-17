package team.rescue.recipe.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import team.rescue.auth.user.PrincipalDetails;
import team.rescue.common.dto.ResponseDto;
import team.rescue.recipe.dto.BookmarkDto.BookmarkInfoDto;
import team.rescue.recipe.dto.RecipeDto.RecipeCreateDto;
import team.rescue.recipe.dto.RecipeDto.RecipeDetailDto;
import team.rescue.recipe.dto.RecipeDto.RecipeInfoDto;
import team.rescue.recipe.dto.RecipeDto.RecipeUpdateDto;
import team.rescue.recipe.service.RecipeService;
import team.rescue.review.dto.ReviewDto.ReviewInfoDto;
import team.rescue.review.service.ReviewService;

@Slf4j
@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

	private final RecipeService recipeService;
	private final ReviewService reviewService;

	/**
	 * 전체 레시피 개수 반환
	 *
	 * @return 전체 레시피 개수
	 */
	@GetMapping
	public ResponseEntity<ResponseDto<Integer>> getTotalRecipeCount() {
		Integer totalRecipeCount = recipeService.getTotalRecipeCount();
		return new ResponseEntity<>(
				new ResponseDto<>(null, totalRecipeCount),
				HttpStatus.OK
		);
	}


	/**
	 * 특정 레시피 상세 조회
	 *
	 * @param recipeId 조회할 레시피 ID
	 * @return 해당 레시피 상세 데이터
	 */
	@GetMapping("/{recipeId}")
	public ResponseEntity<ResponseDto<RecipeDetailDto>> getRecipe(
			@PathVariable Long recipeId,
			@AuthenticationPrincipal PrincipalDetails details
	) {

		RecipeDetailDto recipeDetailDto = recipeService.getRecipe(recipeId, details);
		return new ResponseEntity<>(
				new ResponseDto<>("레시피 조회에 성공하였습니다.", recipeDetailDto),
				HttpStatus.OK
		);
	}

	/**
	 * 레시피 등록
	 *
	 * @param request          레시피 정보
	 * @param recipeImage      레시피 이미지 파일
	 * @param stepImages       단계 별 사진
	 * @param principalDetails 로그인 유저
	 * @return 등록한 레시피 요약 정보
	 */
	@PostMapping
	@PreAuthorize("hasAuthority('USER')")
	public ResponseEntity<ResponseDto<RecipeInfoDto>> addRecipe(
			@RequestPart RecipeCreateDto request,
			@RequestPart MultipartFile recipeImage,
			@RequestPart List<MultipartFile> stepImages,
			@AuthenticationPrincipal PrincipalDetails principalDetails,
			BindingResult bindingResult
	) {

		log.info("[레시피 생성 컨트롤러] email={}, title={}", principalDetails.getMember().getEmail(),
				request.getTitle());

		RecipeInfoDto recipeInfo =
				recipeService.addRecipe(request, recipeImage, stepImages, principalDetails);

		return new ResponseEntity<>(
				new ResponseDto<>("레시피가 성공적으로 등록되었습니다.", recipeInfo),
				HttpStatus.CREATED
		);
	}

	@PatchMapping("/{recipeId}")
	@PreAuthorize("hasAuthority('USER')")
	public ResponseEntity<ResponseDto<RecipeInfoDto>> updateRecipe(
			@PathVariable Long recipeId,
			@RequestPart RecipeUpdateDto request,
			@RequestPart(required = false) MultipartFile recipeImage,
			@RequestPart List<MultipartFile> stepImages,
			@AuthenticationPrincipal PrincipalDetails principalDetails
	) {

		RecipeInfoDto recipeInfoDto =
				recipeService.updateRecipe(recipeId, request, recipeImage, stepImages, principalDetails);

		return new ResponseEntity<>(
				new ResponseDto<>("레시피가 성공적으로 수정되었습니다.", recipeInfoDto),
				HttpStatus.OK
		);
	}

	@DeleteMapping("/{recipeId}")
	@PreAuthorize("hasAuthority('USER')")
	public ResponseEntity<ResponseDto<RecipeInfoDto>> deleteRecipe(
			@PathVariable Long recipeId,
			@AuthenticationPrincipal PrincipalDetails principalDetails) {

		RecipeInfoDto recipeDeleteDto =
				recipeService.deleteRecipe(recipeId, principalDetails);

		return new ResponseEntity<>(
				new ResponseDto<>("레시피가 성공적으로 삭제되었습니다.", recipeDeleteDto),
				HttpStatus.OK
		);
	}

	@PostMapping("/{recipeId}/bookmark")
	@PreAuthorize("hasAuthority('USER')")
	public ResponseEntity<ResponseDto<BookmarkInfoDto>> bookmarkRecipe(
			@PathVariable Long recipeId,
			@AuthenticationPrincipal PrincipalDetails principalDetails
	) {
		BookmarkInfoDto bookmarkInfoDto = recipeService.bookmarkRecipe(recipeId,
				principalDetails.getUsername());

		if (bookmarkInfoDto.getIsBookmarked()) {
			return ResponseEntity.ok(new ResponseDto<>("레시피를 북마크하였습니다.", bookmarkInfoDto));
		} else {
			return ResponseEntity.ok(new ResponseDto<>("레시피 북마크를 취소하였습니다.", bookmarkInfoDto));
		}
	}

	/**
	 * 특정 레시피의 리뷰 목록 조회
	 *
	 * @param recipeId 조회할 레시피 아이디
	 * @param page     리뷰 목록 페이지 번호
	 * @return 리뷰 목록
	 */
	@GetMapping("/{recipeId}/reviews")
	@PreAuthorize("permitAll()")
	public ResponseEntity<ResponseDto<Slice<ReviewInfoDto>>> getRecipeReviews(
			@PathVariable Long recipeId,
			@RequestParam(defaultValue = "0") Integer page
	) {

		// TODO: 이 부분 pageSize는 10으로 고정값인데 다르게 처리할 수 있을지?
		PageRequest pageRequest = PageRequest.of(
				page, 10, Sort.by(Direction.DESC, "createdAt")
		);

		Slice<ReviewInfoDto> recipeReviewList =
				reviewService.getRecipeReviews(recipeId, pageRequest);

		return new ResponseEntity<>(
				new ResponseDto<>(null, recipeReviewList),
				HttpStatus.OK
		);
	}

	@GetMapping("/recent")
	public ResponseEntity<ResponseDto<Page<RecipeInfoDto>>> getRecentRecipes(
			@PageableDefault Pageable pageable
	) {
		Page<RecipeInfoDto> recipeDetailDtoPage = recipeService.getRecentRecipes(pageable);

		return ResponseEntity.ok(new ResponseDto<>("최신 레시피 데이터를 조회했습니다.", recipeDetailDtoPage));
	}

	@GetMapping("/popular")
	public ResponseEntity<ResponseDto<Page<RecipeInfoDto>>> getPopularRecipes(
			@PageableDefault Pageable pageable
	) {
		Page<RecipeInfoDto> recipeDetailDtoPage = recipeService.getPopularRecipes(pageable);

		return ResponseEntity.ok(new ResponseDto<>("인기 레시피 데이터를 조회했습니다.", recipeDetailDtoPage));
	}
}
