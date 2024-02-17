package team.rescue.fridge.service;

import static team.rescue.error.type.AuthError.ACCESS_DENIED;
import static team.rescue.error.type.ServiceError.FRIDGE_NOT_FOUND;
import static team.rescue.error.type.ServiceError.INGREDIENT_NOT_FOUND;
import static team.rescue.error.type.ServiceError.USER_NOT_FOUND;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.rescue.error.exception.AuthException;
import team.rescue.error.exception.ServiceException;
import team.rescue.fridge.dto.FridgeDto;
import team.rescue.fridge.dto.FridgeIngredientDto.FridgeIngredientCreateDto;
import team.rescue.fridge.dto.FridgeIngredientDto.FridgeIngredientInfoDto;
import team.rescue.fridge.dto.FridgeIngredientDto.FridgeIngredientUpdateDto;
import team.rescue.fridge.entity.Fridge;
import team.rescue.fridge.entity.FridgeIngredient;
import team.rescue.fridge.repository.FridgeIngredientRepository;
import team.rescue.fridge.repository.FridgeRepository;
import team.rescue.member.entity.Member;
import team.rescue.member.repository.MemberRepository;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FridgeService {

	private final FridgeRepository fridgeRepository;
	private final MemberRepository memberRepository;
	private final FridgeIngredientRepository fridgeIngredientRepository;

	/**
	 * 냉장고 생성
	 *
	 * @param member 냉장고 소유 유저
	 * @return 생성된 냉장고
	 */
	@Transactional
	public Fridge createFridge(Member member) {

		return fridgeRepository.save(Fridge.builder().member(member).build());
	}

	/**
	 * <p>냉장고 재료를 조회하는 메소드
	 *
	 * @param email 유저 이메일
	 * @return 해당 유저 냉장고 재료 목록
	 */
	public FridgeDto getFridge(String email) {

		Member member = memberRepository.findUserByEmail(email)
				.orElseThrow(() -> new ServiceException(USER_NOT_FOUND));

		Fridge fridge = fridgeRepository.findByMember(member)
				.orElseThrow(() -> new ServiceException(FRIDGE_NOT_FOUND));

		List<FridgeIngredient> fridgeIngredients = fridgeIngredientRepository.findByFridge(fridge);
		List<FridgeIngredientInfoDto> fridgeIngredientInfoList = fridgeIngredients.stream()
				.map(FridgeIngredientInfoDto::of)
				.collect(Collectors.toList());

		return FridgeDto.builder()
				.id(fridge.getId())
				.fridgeIngredientInfoList(fridgeIngredientInfoList)
				.build();
	}

	/**
	 * <p>입력받은 리스트를 순회하면서 냉장고에 재료를 저장
	 * 이름, 메모, 유통기한이 모두 동일한 재료가 두 번 입력되는 경우 재료 등록 처리 하지 않음
	 *
	 * @param email                         유저 이메일
	 * @param fridgeIngredientCreateDtoList 생성할 재료 리스트
	 * @return 생성한 재료 목록
	 */
	@Transactional
	public List<FridgeIngredientInfoDto> addIngredient(
			String email,
			List<FridgeIngredientCreateDto> fridgeIngredientCreateDtoList
	) {

		Member member = memberRepository.findUserByEmail(email)
				.orElseThrow(() -> new ServiceException(USER_NOT_FOUND));

		Fridge fridge = fridgeRepository.findByMember(member)
				.orElseThrow(() -> new ServiceException(FRIDGE_NOT_FOUND));

		for (FridgeIngredientCreateDto fridgeIngredientCreateDto : fridgeIngredientCreateDtoList) {
			if (!fridgeIngredientRepository.existsByNameAndMemoAndExpiredAtAndFridge(
					fridgeIngredientCreateDto.getName(),
					fridgeIngredientCreateDto.getMemo(),
					fridgeIngredientCreateDto.getExpiredAt(),
					fridge)) {

				FridgeIngredient fridgeIngredient = FridgeIngredient.builder()
						.fridge(fridge)
						.name(fridgeIngredientCreateDto.getName())
						.memo(fridgeIngredientCreateDto.getMemo())
						.expiredAt(fridgeIngredientCreateDto.getExpiredAt())
						.build();

				fridgeIngredientRepository.save(fridgeIngredient);
			}
		}

		List<FridgeIngredient> fridgeIngredients = fridgeIngredientRepository.findByFridge(fridge);
		return fridgeIngredients.stream().map(FridgeIngredientInfoDto::of)
				.collect(Collectors.toList());
	}

	@Transactional
	public List<FridgeIngredientInfoDto> updateIngredient(String email,
			FridgeIngredientUpdateDto fridgeIngredientUpdateDto) {
		Member member = memberRepository.findUserByEmail(email)
				.orElseThrow(() -> new ServiceException(USER_NOT_FOUND));

		Fridge fridge = fridgeRepository.findByMember(member)
				.orElseThrow(() -> new ServiceException(FRIDGE_NOT_FOUND));

		List<Long> deleteItemList = fridgeIngredientUpdateDto.getDelete();
		deleteIngredient(deleteItemList, fridge);

		List<FridgeIngredientInfoDto> updateItemList = fridgeIngredientUpdateDto.getUpdate();
		modifyIngredient(updateItemList, fridge);

		List<FridgeIngredient> fridgeIngredientList = fridgeIngredientRepository.findByFridge(
				fridge);

		return fridgeIngredientList.stream().map(FridgeIngredientInfoDto::of)
				.collect(Collectors.toList());
	}

	private void deleteIngredient(List<Long> deleteItemList, Fridge fridge) {
			for (Long id : deleteItemList) {
				FridgeIngredient fridgeIngredient = fridgeIngredientRepository.findById(id)
						.orElseThrow(() -> new ServiceException(INGREDIENT_NOT_FOUND));

				if (fridgeIngredient.getFridge() != fridge) {
					throw new AuthException(ACCESS_DENIED);
				}

				fridgeIngredientRepository.deleteById(id);
			}
	}

	private void modifyIngredient(List<FridgeIngredientInfoDto> updateItemList, Fridge
			fridge) {
		for (FridgeIngredientInfoDto fridgeIngredientInfoDto : updateItemList) {
			FridgeIngredient fridgeIngredient = fridgeIngredientRepository.findById(
							fridgeIngredientInfoDto.getId())
					.orElseThrow(() -> new ServiceException(INGREDIENT_NOT_FOUND));

			if (fridgeIngredient.getFridge() != fridge) {
				throw new AuthException(ACCESS_DENIED);
			}

			fridgeIngredient.updateFridgeIngredient(
					fridgeIngredientInfoDto.getName(),
					fridgeIngredientInfoDto.getMemo(),
					fridgeIngredientInfoDto.getExpiredAt());

			fridgeIngredientRepository.save(fridgeIngredient);
		}
	}
}
