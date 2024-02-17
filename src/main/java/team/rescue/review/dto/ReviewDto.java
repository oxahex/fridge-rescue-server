package team.rescue.review.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import team.rescue.member.dto.MemberDto.MemberInfoDto;
import team.rescue.recipe.dto.RecipeDto.RecipeInfoDto;
import team.rescue.review.entity.Review;

public class ReviewDto {

	@Getter
	@Setter
	public static class ReviewReqDto {

		// TODO: Valid 추가 필요

		private Long recipeId;
		private Long cookId;
		private String title;
		private String contents;
	}

	@Getter
	@Setter
	public static class ReviewUpdateDto {

		private String title;
		private String contents;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ReviewInfoDto {

		private Long id;
		private String title;
		private String contents;
		private String imageUrl;
		private MemberInfoDto author;
		private LocalDateTime createdAt;

		public static ReviewInfoDto of(Review review) {

			ReviewInfoDto reviewInfo = new ReviewInfoDto();
			reviewInfo.setId(review.getId());
			reviewInfo.setTitle(review.getTitle());
			reviewInfo.setContents(review.getContents());
			reviewInfo.setImageUrl(review.getImageUrl());
			reviewInfo.setAuthor(MemberInfoDto.of(review.getMember()));
			reviewInfo.setCreatedAt(review.getCreatedAt());

			return reviewInfo;
		}

	}

	@Getter
	@Setter
	public static class ReviewDetailDto {

		private Long id;
		private String title;
		private String imageUrl;
		private String contents;
		private MemberInfoDto author;
		private RecipeInfoDto recipe;
		private LocalDateTime createdAt;

		public static ReviewDetailDto of(Review review) {

			ReviewDetailDto reviewDetail = new ReviewDetailDto();
			reviewDetail.setId(review.getId());
			reviewDetail.setTitle(review.getTitle());
			reviewDetail.setImageUrl(review.getImageUrl());
			reviewDetail.setContents(review.getContents());
			reviewDetail.setAuthor(MemberInfoDto.of(review.getMember()));
			reviewDetail.setRecipe(RecipeInfoDto.of(review.getRecipe()));
			reviewDetail.setCreatedAt(review.getCreatedAt());

			return reviewDetail;
		}

	}

}
