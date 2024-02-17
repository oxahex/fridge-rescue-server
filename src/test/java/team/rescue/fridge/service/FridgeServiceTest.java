package team.rescue.fridge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static team.rescue.error.type.ServiceError.FRIDGE_NOT_FOUND;
import static team.rescue.error.type.ServiceError.INGREDIENT_NOT_FOUND;
import static team.rescue.error.type.ServiceError.USER_NOT_FOUND;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.rescue.auth.type.RoleType;
import team.rescue.error.exception.AuthException;
import team.rescue.error.exception.ServiceException;
import team.rescue.error.type.AuthError;
import team.rescue.fridge.dto.FridgeDto;
import team.rescue.fridge.dto.FridgeIngredientDto.FridgeIngredientInfoDto;
import team.rescue.fridge.dto.FridgeIngredientDto.FridgeIngredientUpdateDto;
import team.rescue.fridge.entity.Fridge;
import team.rescue.fridge.entity.FridgeIngredient;
import team.rescue.fridge.repository.FridgeIngredientRepository;
import team.rescue.fridge.repository.FridgeRepository;
import team.rescue.member.entity.Member;
import team.rescue.member.repository.MemberRepository;
import team.rescue.mock.WithMockMember;

@ExtendWith(MockitoExtension.class)
class FridgeServiceTest {

	@Mock
	FridgeRepository fridgeRepository;

	@Mock
	MemberRepository memberRepository;

	@Mock
	FridgeIngredientRepository fridgeIngredientRepository;

	@InjectMocks
	FridgeService fridgeService;

	@Test
	@DisplayName("냉장고 생성 성공")
	@WithMockMember(role = RoleType.USER)
	void successCreateFridge() {
		// given
		Member member = Member.builder()
				.email("test@gmail.com")
				.build();

		given(fridgeRepository.save(any(Fridge.class)))
				.willReturn(Fridge.builder()
						.member(member)
						.build());

		// when
		Fridge fridge = fridgeService.createFridge(member);

		// then
		assertEquals("test@gmail.com", fridge.getMember().getEmail());
	}

	@Test
	@DisplayName("냉장고 재료 조회 성공")
	@WithMockMember(role = RoleType.USER)
	void successGetFridge() {
		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트")
				.email("test@gmail.com")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		Fridge fridge = Fridge.builder()
				.id(1L)
				.member(member)
				.build();

		given(fridgeRepository.findByMember(member))
				.willReturn(Optional.of(fridge));

		FridgeIngredient fridgeIngredient1 = FridgeIngredient.builder()
				.fridge(fridge)
				.name("양파")
				.memo("하얀 양파")
				.build();
		FridgeIngredient fridgeIngredient2 = FridgeIngredient.builder()
				.fridge(fridge)
				.name("당근")
				.memo("주황 당근")
				.build();

		List<FridgeIngredient> fridgeIngredients = new ArrayList<>(
				Arrays.asList(fridgeIngredient1, fridgeIngredient2));

		given(fridgeIngredientRepository.findByFridge(fridge))
				.willReturn(fridgeIngredients);

		// when
		FridgeDto fridgeDto = fridgeService.getFridge("test@gmail.com");

		// then
		assertEquals(1L, fridgeDto.getId());
		assertEquals("양파", fridgeDto.getFridgeIngredientInfoList().get(0).getName());
		assertEquals("당근", fridgeDto.getFridgeIngredientInfoList().get(1).getName());
	}

	@Test
	@DisplayName("냉장고 재료 조회 실패 - 사용자 정보 없음")
	@WithMockMember(role = RoleType.USER)
	void failGetFridge_UserNotFound() {
		// given
		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> fridgeService.getFridge("test@gmail.com"));

		// then
		assertEquals(USER_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("냉장고 재료 조회 실패 - 냉장고 정보 없음")
	@WithMockMember(role = RoleType.USER)
	void failGetFridge_FridgeNotFound() {
		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트")
				.email("test@gmail.com")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		given(fridgeRepository.findByMember(member))
				.willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> fridgeService.getFridge("test@gmail.com"));

		// then
		assertEquals(FRIDGE_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("냉장고 재료 등록 성공")
	@WithMockMember(role = RoleType.USER)
	void successAddIngredient() {
		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트")
				.email("test@gmail.com")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		Fridge fridge = Fridge.builder()
				.id(1L)
				.member(member)
				.build();

		given(fridgeRepository.findByMember(member))
				.willReturn(Optional.of(fridge));

		FridgeIngredient fridgeIngredient1 = FridgeIngredient.builder()
				.fridge(fridge)
				.name("양파")
				.memo("하얀 양파")
				.build();
		FridgeIngredient fridgeIngredient2 = FridgeIngredient.builder()
				.fridge(fridge)
				.name("당근")
				.memo("주황 당근")
				.build();

		List<FridgeIngredient> fridgeIngredients = new ArrayList<>(
				Arrays.asList(fridgeIngredient1, fridgeIngredient2));

		given(fridgeIngredientRepository.findByFridge(fridge))
				.willReturn(fridgeIngredients);

		// when
		List<FridgeIngredientInfoDto> fridgeIngredientInfoDtoList = fridgeService.addIngredient(
				"test@gmail.com", anyList());

		// then
		assertEquals("양파", fridgeIngredientInfoDtoList.get(0).getName());
		assertEquals("당근", fridgeIngredientInfoDtoList.get(1).getName());
	}

	@Test
	@DisplayName("냉장고 재료 등록 실패 - 사용자 정보 없음")
	void failAddIngredient_UserNotFound() {
		// given
		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> fridgeService.addIngredient("test@gmail.com", any()));

		// then
		assertEquals(USER_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("냉장고 재료 등록 실패 - 냉장고 정보 없음")
	void failAddIngredient_FridgeNotFound() {
		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트")
				.email("test@gmail.com")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		given(fridgeRepository.findByMember(member))
				.willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> fridgeService.addIngredient("test@gmail.com", any()));

		// then
		assertEquals(FRIDGE_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("냉장고 재료 수정 및 삭제 성공")
	@WithMockMember(role = RoleType.USER)
	void successUpdateIngredient() {
		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트")
				.email("test@gmail.com")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		Fridge fridge = Fridge.builder()
				.id(1L)
				.member(member)
				.build();

		given(fridgeRepository.findByMember(member))
				.willReturn(Optional.of(fridge));

		FridgeIngredientInfoDto fridgeIngredientInfoDto = FridgeIngredientInfoDto.builder()
				.id(3L)
				.name("당근")
				.memo("주황 당근")
				.build();

		FridgeIngredientUpdateDto fridgeIngredientUpdateDto = FridgeIngredientUpdateDto.builder()
				.deleteItem(List.of(1L))
				.updateItem(List.of(fridgeIngredientInfoDto))
				.build();

		given(fridgeIngredientRepository.findById(fridgeIngredientUpdateDto.getDeleteItem().get(0)))
				.willReturn(Optional.of(FridgeIngredient.builder()
						.id(1L)
						.name("양파")
						.fridge(fridge)
						.build()));

		given(fridgeIngredientRepository.findById(
				fridgeIngredientUpdateDto.getUpdateItem().get(0).getId()))
				.willReturn(Optional.of(FridgeIngredient.builder()
						.name("오이")
						.memo("초록 오이")
						.fridge(fridge)
						.build()));

		FridgeIngredient fridgeIngredient1 = FridgeIngredient.builder()
				.fridge(fridge)
				.name("당근")
				.memo("주황 당근")
				.build();

		List<FridgeIngredient> fridgeIngredients = new ArrayList<>(
				List.of(fridgeIngredient1));

		given(fridgeIngredientRepository.findByFridge(fridge))
				.willReturn(fridgeIngredients);

		// when
		List<FridgeIngredientInfoDto> fridgeIngredientInfoDtoList = fridgeService.updateIngredient(
				"test@gmail.com", fridgeIngredientUpdateDto);

		// then
		verify(fridgeIngredientRepository, times(1)).deleteById(1L);
		verify(fridgeIngredientRepository, times(1)).save(any());

		assertEquals(1, fridgeIngredientInfoDtoList.size());
		assertEquals("당근", fridgeIngredientInfoDtoList.get(0).getName());
	}

	@Test
	@DisplayName("냉장고 재료 수정 및 삭제 실패 - 사용자 정보 없음")
	@WithMockMember(role = RoleType.USER)
	void failUpdateIngredient_UserNotFound() {
		// given
		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> fridgeService.updateIngredient("test@gmail.com", any()));

		// then
		assertEquals(USER_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("냉장고 재료 수정 및 삭제 실패 - 냉장고 정보 없음")
	void failUpdateIngredient_FridgeNotFound() {
		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트")
				.email("test@gmail.com")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		given(fridgeRepository.findByMember(member))
				.willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> fridgeService.updateIngredient("test@gmail.com", any()));

		// then
		assertEquals(FRIDGE_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("냉장고 재료 수정 및 삭제 실패 - 재료 정보 없음")
	@WithMockMember(role = RoleType.USER)
	void failUpdateIngredient_IngredientNotFound() {
		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트")
				.email("test@gmail.com")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		Fridge fridge = Fridge.builder()
				.id(1L)
				.member(member)
				.build();

		given(fridgeRepository.findByMember(member))
				.willReturn(Optional.of(fridge));

		given(fridgeIngredientRepository.findById(anyLong()))
				.willReturn(Optional.empty());

		FridgeIngredientInfoDto fridgeIngredientInfoDto = FridgeIngredientInfoDto.builder()
				.id(3L)
				.name("당근")
				.memo("주황 당근")
				.build();

		FridgeIngredientUpdateDto fridgeIngredientUpdateDto = FridgeIngredientUpdateDto.builder()
				.deleteItem(List.of(1L))
				.updateItem(List.of(fridgeIngredientInfoDto))
				.build();

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> fridgeService.updateIngredient("test@gmail.com", fridgeIngredientUpdateDto));

		// then
		assertEquals(INGREDIENT_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("냉장고 재료 수정 및 삭제 실패 - 권한이 없음")
	@WithMockMember(role = RoleType.USER)
	void failUpdateIngredient_AccessDenied() {
		// given
		Member member = Member.builder()
				.id(1L)
				.nickname("테스트")
				.email("test@gmail.com")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		Fridge fridge = Fridge.builder()
				.id(1L)
				.member(member)
				.build();

		given(fridgeRepository.findByMember(member))
				.willReturn(Optional.of(fridge));

		given(fridgeIngredientRepository.findById(anyLong()))
				.willReturn(Optional.empty());

		FridgeIngredientInfoDto fridgeIngredientInfoDto = FridgeIngredientInfoDto.builder()
				.id(3L)
				.name("당근")
				.memo("주황 당근")
				.build();

		FridgeIngredientUpdateDto fridgeIngredientUpdateDto = FridgeIngredientUpdateDto.builder()
				.deleteItem(List.of(1L))
				.updateItem(List.of(fridgeIngredientInfoDto))
				.build();

		given(fridgeIngredientRepository.findById(fridgeIngredientUpdateDto.getDeleteItem().get(0)))
				.willReturn(Optional.of(FridgeIngredient.builder()
						.id(1L)
						.name("양파")
						.build()));

		// when
		AuthException authException = assertThrows(AuthException.class,
				() -> fridgeService.updateIngredient("test@gmail.com", fridgeIngredientUpdateDto));

		// then
		assertEquals(AuthError.ACCESS_DENIED.getHttpStatus(), authException.getHttpStatus());

	}

}
