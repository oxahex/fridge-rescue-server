package team.rescue.cook.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import team.rescue.cook.entity.Cook;
import team.rescue.fridge.dto.FridgeIngredientDto.FridgeIngredientUseDto;
import team.rescue.recipe.dto.RecipeDto.RecipeInfoDto;

public class CookDto {

	@Getter
	@Setter
	@Builder
	public static class CookInfoDto {

		private Long id;
		private LocalDateTime createdAt;
		private RecipeInfoDto recipeInfoDto;

		public static CookInfoDto of(Cook cook) {
			return CookInfoDto.builder()
					.id(cook.getId())
					.createdAt(cook.getCreatedAt())
					.recipeInfoDto(RecipeInfoDto.of(cook.getRecipe()))
					.build();
		}
	}

	@Getter
	@Setter
	@Builder
	public static class CookCreateDto {

		private Long recipeId;
		private List<Long> delete;
		private List<FridgeIngredientUseDto> update;
	}

}
