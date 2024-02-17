package team.rescue.review.service;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import team.rescue.aop.DistributedLock;
import team.rescue.auth.user.PrincipalDetails;
import team.rescue.common.file.FileService;
import team.rescue.cook.entity.Cook;
import team.rescue.cook.repository.CookRepository;
import team.rescue.error.exception.ServiceException;
import team.rescue.error.type.ServiceError;
import team.rescue.member.entity.Member;
import team.rescue.member.repository.MemberRepository;
import team.rescue.notification.entity.NotificationProperty;
import team.rescue.notification.event.NotificationEvent;
import team.rescue.notification.event.NotificationEventPublisher;
import team.rescue.notification.type.NotificationType;
import team.rescue.recipe.entity.Recipe;
import team.rescue.recipe.repository.RecipeRepository;
import team.rescue.review.dto.ReviewDto.ReviewDetailDto;
import team.rescue.review.dto.ReviewDto.ReviewInfoDto;
import team.rescue.review.dto.ReviewDto.ReviewReqDto;
import team.rescue.review.dto.ReviewDto.ReviewUpdateDto;
import team.rescue.review.entity.Review;
import team.rescue.review.repository.ReviewRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final MemberRepository memberRepository;
	private final CookRepository cookRepository;
	private final RecipeRepository recipeRepository;
	private final FileService fileService;
	private final NotificationEventPublisher notificationEventPublisher;


	/**
	 * 리뷰 등록
	 *
	 * @param reviewReqDto 리뷰 등록 요청 데이터
	 * @param image        리뷰 이미지 파일
	 * @param details      Principal
	 * @return 등록 리뷰 요약 데이터 DTO
	 */
	@Transactional
	@DistributedLock(prefix = "review_recipe")
	public ReviewInfoDto createReview(
			ReviewReqDto reviewReqDto,
			MultipartFile image,
			PrincipalDetails details
	) {

		log.info("[리뷰 생성]");

		Member member = memberRepository.findUserByEmail(details.getMember().getEmail())
				.orElseThrow(() -> new ServiceException(ServiceError.USER_NOT_FOUND));

		Cook cook = cookRepository.getReferenceById(reviewReqDto.getCookId());
		Recipe recipe = recipeRepository.getReferenceById(reviewReqDto.getRecipeId());

		recipe.increaseReviewCount();

		Review review = Review.builder()
				.member(member)
				.recipe(recipe)
				.cook(cook)
				.title(reviewReqDto.getTitle())
				.contents(reviewReqDto.getContents())
				.imageUrl(fileService.uploadImageToS3(image))
				.build();

		// 레시피 리뷰에 대한 이벤트 발행
		NotificationEvent event = NotificationEvent.builder()
				.email(recipe.getMember().getEmail())
				.notificationType(NotificationType.RECIPE_REVIEWED)
				.notificationProperty(NotificationProperty.builder()
						.contents(member.getNickname() + "님이 " + recipe.getTitle() + " 게시글에 리뷰를 남겼습니다.")
						.originId(recipe.getId())
						.originUserId(member.getId())
						.build())
				.build();
		notificationEventPublisher.publishEvent(event);

		return ReviewInfoDto.of(reviewRepository.save(review));
	}

	/**
	 * 특정 리뷰 상세 조회
	 *
	 * @param reviewId 조회할 리뷰 ID
	 * @return 리뷰 상세 DTO
	 */
	@Transactional(readOnly = true)
	public ReviewDetailDto getReview(Long reviewId) {

		log.info("[리뷰 상세 조회]");
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new ServiceException(ServiceError.REVIEW_NOT_FOUND));

		return ReviewDetailDto.of(review);
	}

	/**
	 * 리뷰 내용 수정
	 *
	 * @param email    리뷰 수정 요정 유저 이메일
	 * @param reviewId 수정할 리뷰 아이디
	 * @param request  수정할 내용
	 * @return 수정된 리뷰 데이터
	 */
	@Transactional
	public ReviewInfoDto updateReview(
			String email,
			Long reviewId,
			ReviewUpdateDto request,
			MultipartFile image
	) {

		log.info("[리뷰 수정] reviewId={}", reviewId);
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new ServiceException(ServiceError.REVIEW_NOT_FOUND));

		// 리뷰 수정 요청 유저와 리뷰 작성 유저 동일인 검증
		validateReviewAuthor(email, review);

		if (image != null) {
			// S3 이미지 수정
			String imageUrl = fileService.uploadImageToS3(image);

			// 기존 이미지 있는 경우 삭제
			if (review.getImageUrl() != null) {
				fileService.deleteImages(review.getImageUrl());
			}
			// 리뷰 업데이트
			review.update(request.getTitle(), imageUrl, request.getContents());
		} else {
			review.updateWithoutImage(request.getTitle(), request.getContents());
		}

		return ReviewInfoDto.of(reviewRepository.save(review));
	}

	/**
	 * 리뷰 삭제
	 *
	 * @param email    리뷰 삭제 요청 유저 이메일
	 * @param reviewId 삭제할 리뷰 아이디
	 * @return 삭제된 리뷰 데이터
	 */
	@Transactional
	public ReviewInfoDto deleteReview(String email, Long reviewId) {

		log.info("[리뷰 삭제] reviewId={}", reviewId);
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new ServiceException(ServiceError.REVIEW_NOT_FOUND));

		// 요청 유저와 삭제할 리뷰 작성자가 동일인인지 검증
		validateReviewAuthor(email, review);

		// 레시피 리뷰 수 감소
		Recipe recipe = review.getRecipe();
		recipe.decreaseReviewCount();

		// 리뷰 데이터 삭제
		reviewRepository.delete(review);
		recipeRepository.save(recipe);

		return ReviewInfoDto.of(review);
	}

	public Slice<ReviewInfoDto> getRecipeReviews(
			Long recipeId,
			PageRequest pageRequest
	) {

		log.info("[레시피 리뷰 목록 조회] recipeId={}", recipeId);

		// 해당 레시피 아이디로 리뷰 조회
		Slice<Review> reviewList = reviewRepository.findReviewsByRecipeId(recipeId, pageRequest);

		return reviewList.map(ReviewInfoDto::of);
	}

	/**
	 * 요청 유저와 리뷰 실제 작성 유저 동일성 검증
	 *
	 * @param email  요청 유저 email
	 * @param review 검증하려는 리뷰 객체
	 */
	private void validateReviewAuthor(String email, Review review) {
		if (!Objects.equals(review.getMember().getEmail(), email)) {
			throw new ServiceException(ServiceError.REVIEW_MEMBER_UNMATCHED);
		}
	}
}
