package team.rescue.review.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import team.rescue.auth.user.PrincipalDetails;
import team.rescue.common.dto.ResponseDto;
import team.rescue.review.dto.ReviewDto.ReviewDetailDto;
import team.rescue.review.dto.ReviewDto.ReviewInfoDto;
import team.rescue.review.dto.ReviewDto.ReviewReqDto;
import team.rescue.review.dto.ReviewDto.ReviewUpdateDto;
import team.rescue.review.service.ReviewService;

@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;

	/**
	 * 리뷰 등록
	 *
	 * @param request 리뷰 등록 요청 데이터
	 * @param image   리뷰 사진
	 * @param details 로그인 유저
	 * @return 생성된 리뷰 요약 데이터
	 */
	@PostMapping
	@PreAuthorize("hasAuthority('USER')")
	public ResponseEntity<ResponseDto<ReviewInfoDto>> createReview(
			@RequestPart ReviewReqDto request,
			BindingResult bindingResult,
			@RequestPart(required = false) MultipartFile image,
			@AuthenticationPrincipal PrincipalDetails details
	) {

		log.info("[리뷰 업로드] recipeId={}, title={}, imageOriginFileName={}", request.getRecipeId(),
				request.getTitle(), image.getOriginalFilename());

		ReviewInfoDto reviewInfo = reviewService.createReview(request, image, details);

		return new ResponseEntity<>(
				new ResponseDto<>("레시피 리뷰가 등록되었습니다.", reviewInfo),
				HttpStatus.CREATED
		);

	}

	/**
	 * 특정 리뷰 상세 조회
	 *
	 * @param reviewId 조회할 리뷰 아이디
	 * @return 해당 리뷰 상세 DTO
	 */
	@GetMapping("/{reviewId}")
	@PreAuthorize("permitAll()")
	public ResponseEntity<ResponseDto<ReviewDetailDto>> getReviewDetail(
			@PathVariable Long reviewId
	) {

		ReviewDetailDto reviewDetailDto = reviewService.getReview(reviewId);

		return new ResponseEntity<>(
				new ResponseDto<>(null, reviewDetailDto),
				HttpStatus.OK
		);
	}

	/**
	 * 리뷰 수정
	 *
	 * @param reviewId      수정할 리뷰 아이디
	 * @param request       수정 내용
	 * @param bindingResult 유효성 검증 오류
	 * @param details       로그인 유저 Principal
	 * @return 수정된 리뷰 데이터
	 */
	@PatchMapping("/{reviewId}")
	@PreAuthorize("hasAuthority('USER')")
	public ResponseEntity<ResponseDto<ReviewInfoDto>> updateReview(
			@PathVariable Long reviewId,
			@RequestPart ReviewUpdateDto request,
			BindingResult bindingResult,
			@RequestPart(required = false) MultipartFile image,
			@AuthenticationPrincipal PrincipalDetails details
	) {

		ReviewInfoDto reviewInfoDto = reviewService.updateReview(
				details.getUsername(),
				reviewId,
				request,
				image
		);

		return new ResponseEntity<>(
				new ResponseDto<>("리뷰를 수정했습니다.", reviewInfoDto),
				HttpStatus.OK
		);
	}

	/**
	 * 특정 리뷰 삭제
	 *
	 * @param reviewId 삭제할 리뷰 아이디
	 * @return 삭제한 리뷰 정보 반환
	 */
	@DeleteMapping("/{reviewId}")
	@PreAuthorize("hasAuthority('USER')")
	public ResponseEntity<ResponseDto<ReviewInfoDto>> deleteReview(
			@PathVariable Long reviewId,
			@AuthenticationPrincipal PrincipalDetails details
	) {

		ReviewInfoDto reviewInfoDto =
				reviewService.deleteReview(details.getUsername(), reviewId);

		return new ResponseEntity<>(
				new ResponseDto<>("리뷰를 삭제했습니다.", reviewInfoDto),
				HttpStatus.OK
		);
	}
}
