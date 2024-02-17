package team.rescue.recipe.dto;

import lombok.Getter;
import lombok.Setter;
import team.rescue.recipe.entity.RecipeIngredient;

public class RecipeIngredientDto {

	@Getter
	@Setter
	public static class RecipeIngredientCreateDto {

		private String name;
		private String amount;

		public static RecipeIngredientCreateDto of(RecipeIngredient recipeIngredient) {
			RecipeIngredientCreateDto recipeIngredientCreateDto = new RecipeIngredientCreateDto();
			recipeIngredientCreateDto.setName(recipeIngredient.getName());
			recipeIngredientCreateDto.setAmount(recipeIngredient.getAmount());

			return recipeIngredientCreateDto;
		}
	}

	@Getter
	@Setter
	public static class RecipeIngredientInfoDto {

		private Long id;
		private String name;
		private String amount;

		public static RecipeIngredientInfoDto of(RecipeIngredient recipeIngredient) {
			RecipeIngredientInfoDto recipeIngredientInfoDto = new RecipeIngredientInfoDto();
			recipeIngredientInfoDto.setId(recipeIngredient.getId());
			recipeIngredientInfoDto.setName(recipeIngredient.getName());
			recipeIngredientInfoDto.setAmount(recipeIngredient.getAmount());

			return recipeIngredientInfoDto;
		}
	}

}
