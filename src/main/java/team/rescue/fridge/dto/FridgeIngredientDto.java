package team.rescue.fridge.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import team.rescue.fridge.entity.FridgeIngredient;

public class FridgeIngredientDto {

	@Getter
	@Setter
	@Builder
	public static class FridgeIngredientCreateDto {

		private String name;

		private String memo;

		@FutureOrPresent(message = "유통기한이 이미 지난 재료입니다.")
		private LocalDate expiredAt;
	}

	@Getter
	@Setter
	@Builder
	public static class FridgeIngredientInfoDto {

		private Long id;
		private String name;
		private String memo;

		@FutureOrPresent(message = "유통기한이 이미 지난 재료입니다.")
		private LocalDate expiredAt;

		public static FridgeIngredientInfoDto of(FridgeIngredient fridgeIngredient) {
			return FridgeIngredientInfoDto.builder()
					.id(fridgeIngredient.getId())
					.name(fridgeIngredient.getName())
					.memo(fridgeIngredient.getMemo())
					.expiredAt(fridgeIngredient.getExpiredAt())
					.build();
		}
	}

	@Getter
	@Setter
	@Builder
	public static class FridgeIngredientUpdateDto {

		private List<Long> delete;
		@Valid
		private List<FridgeIngredientInfoDto> update;
	}

	@Getter
	@Setter
	@Builder
	public static class FridgeIngredientUseDto {

		private Long id;
		private String memo;
	}

}
