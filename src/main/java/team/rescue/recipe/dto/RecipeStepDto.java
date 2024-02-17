package team.rescue.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import team.rescue.recipe.entity.RecipeStep;

@Getter
@Builder
public class RecipeStepDto {

	// 레시피 스탭 생성 DTO
	@Getter
	@Setter
	@Builder
	public static class RecipeStepCreateDto {

		private String description;
		private String tip;

	}

	// 레시피 스탭 조회 DTO
	@Getter
	@Builder
	public static class RecipeStepInfoDto {

		private Long id;
		private int stepNo;
		private String stepImageUrl;
		private String stepDescription;
		private String stepTip;

		public static RecipeStepInfoDto of(RecipeStep recipeStep) {
			return RecipeStepInfoDto.builder()
					.id(recipeStep.getId())
					.stepNo(recipeStep.getStepNo())
					.stepImageUrl(recipeStep.getStepImageUrl())
					.stepDescription(recipeStep.getStepDescription())
					.stepTip(recipeStep.getStepTip())
					.build();
		}
	}

	// 레시피 스탭 업데이트 DTO
	@Getter
	@Setter
	@Builder
	public static class RecipeStepUpdateDto {

		private Long id;
		private int stepNo;
		private String description;
		private String tip;

	}

	// 레시피 스탭 삭제 DTO
	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class RecipeStepDeleteDto {

		private Long id;

	}
}
