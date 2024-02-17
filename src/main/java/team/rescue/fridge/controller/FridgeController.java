package team.rescue.fridge.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.rescue.aop.ListValidation;
import team.rescue.auth.user.PrincipalDetails;
import team.rescue.common.dto.ResponseDto;
import team.rescue.fridge.dto.FridgeDto;
import team.rescue.fridge.dto.FridgeIngredientDto.FridgeIngredientCreateDto;
import team.rescue.fridge.dto.FridgeIngredientDto.FridgeIngredientInfoDto;
import team.rescue.fridge.dto.FridgeIngredientDto.FridgeIngredientUpdateDto;
import team.rescue.fridge.service.FridgeService;

@Slf4j
@RestController
@RequestMapping("/api/fridge")
@RequiredArgsConstructor
public class FridgeController {

	private final FridgeService fridgeService;

	/**
	 * 냉장고 조회
	 *
	 * @param principalDetails 로그인 유저
	 * @return 냉장고 및 포함된 재료 리스트
	 */
	@GetMapping
	@PreAuthorize("hasAuthority('USER')")
	public ResponseEntity<ResponseDto<FridgeDto>> getFridge(
			@AuthenticationPrincipal PrincipalDetails principalDetails
	) {

		String email = principalDetails.getUsername();
		FridgeDto fridgeDto = fridgeService.getFridge(email);
		return ResponseEntity.ok(new ResponseDto<>(null, fridgeDto));
	}

	/**
	 * 냉장고 재료 등록
	 *
	 * @param fridgeIngredientCreateDtoList 등록할 재료 목록
	 * @param principalDetails              로그인 유저
	 * @return 등록한 재료 목록
	 */
	@PostMapping("/ingredients")
	@PreAuthorize("hasAuthority('USER')")
	@ListValidation
	public ResponseEntity<ResponseDto<List<FridgeIngredientInfoDto>>> addIngredient(
			@RequestBody List<FridgeIngredientCreateDto> fridgeIngredientCreateDtoList,
			BindingResult bindingResult,
			@AuthenticationPrincipal PrincipalDetails principalDetails
	) {

		String email = principalDetails.getUsername();

		List<FridgeIngredientInfoDto> fridgeIngredientInfoDtoList = fridgeService.addIngredient(email,
				fridgeIngredientCreateDtoList);
		return ResponseEntity.ok(
				new ResponseDto<>(null, fridgeIngredientInfoDtoList));
	}

	@PatchMapping("/ingredients")
	@PreAuthorize("hasAuthority('USER')")
	public ResponseEntity<ResponseDto<List<FridgeIngredientInfoDto>>> updateIngredient(
			@RequestBody @Valid FridgeIngredientUpdateDto fridgeIngredientUpdateDto,
			BindingResult bindingResult,
			@AuthenticationPrincipal PrincipalDetails principalDetails
	) {

		String email = principalDetails.getUsername();
		List<FridgeIngredientInfoDto> fridgeIngredientInfoDtoList =
				fridgeService.updateIngredient(email, fridgeIngredientUpdateDto);

		return ResponseEntity.ok(new ResponseDto<>(null, fridgeIngredientInfoDtoList));
	}

}
