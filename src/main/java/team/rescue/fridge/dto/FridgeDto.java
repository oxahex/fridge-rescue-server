package team.rescue.fridge.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import team.rescue.fridge.dto.FridgeIngredientDto.FridgeIngredientInfoDto;

@Getter
@Setter
@Builder
public class FridgeDto {

	private Long id;
	private List<FridgeIngredientInfoDto> fridgeIngredientInfoList;
}
