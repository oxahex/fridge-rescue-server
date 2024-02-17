package team.rescue.cook.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static team.rescue.error.type.ServiceError.INGREDIENT_NOT_FOUND;
import static team.rescue.error.type.ServiceError.RECIPE_NOT_FOUND;
import static team.rescue.error.type.ServiceError.USER_NOT_FOUND;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.rescue.auth.type.RoleType;
import team.rescue.cook.dto.CookDto.CookCreateDto;
import team.rescue.cook.dto.CookDto.CookInfoDto;
import team.rescue.cook.entity.Cook;
import team.rescue.cook.repository.CookRepository;
import team.rescue.error.exception.ServiceException;
import team.rescue.fridge.dto.FridgeIngredientDto.FridgeIngredientUseDto;
import team.rescue.fridge.entity.Fridge;
import team.rescue.fridge.entity.FridgeIngredient;
import team.rescue.fridge.repository.FridgeIngredientRepository;
import team.rescue.fridge.repository.FridgeRepository;
import team.rescue.member.entity.Member;
import team.rescue.member.repository.MemberRepository;
import team.rescue.mock.WithMockMember;
import team.rescue.recipe.entity.Recipe;
import team.rescue.recipe.repository.RecipeRepository;

@ExtendWith(MockitoExtension.class)
class CookServiceTest {

	@Mock
	MemberRepository memberRepository;

	@Mock
	RecipeRepository recipeRepository;

	@Mock
	CookRepository cookRepository;

	@Mock
	FridgeIngredientRepository fridgeIngredientRepository;

	@Mock
	FridgeRepository fridgeRepository;

	@InjectMocks
	CookService cookService;

	@Test
	@DisplayName("요리 완료 성공")
	@WithMockMember(role = RoleType.USER)
	void successCompleteCook() {
		// given
		Member member = Member.builder()
				.id(1L)
				.email("test@gmail.com")
				.build();

		Fridge fridge = Fridge.builder()
				.id(1L)
				.member(member)
				.build();

		Recipe recipe = Recipe.builder()
				.id(1L)
				.title("양파 볶음")
				.member(member)
				.build();

		FridgeIngredientUseDto fridgeIngredientUseDto = FridgeIngredientUseDto.builder()
				.id(1L)
				.memo("양파 2개")
				.build();

		List<FridgeIngredientUseDto> list = new ArrayList<>(
				Collections.singletonList(fridgeIngredientUseDto));

		FridgeIngredient fridgeIngredient = FridgeIngredient.builder()
				.fridge(fridge)
				.id(1L)
				.name("양파")
				.memo("양파 3개")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		given(fridgeIngredientRepository.findById(anyLong()))
				.willReturn(Optional.of(fridgeIngredient));

		FridgeIngredient updatedFridgeIngredient = FridgeIngredient.builder()
				.id(1L)
				.name("양파")
				.memo("양파 2개")
				.build();

		given(fridgeIngredientRepository.save(any()))
				.willReturn(updatedFridgeIngredient);

		given(recipeRepository.findById(1L))
				.willReturn(Optional.of(recipe));

		Cook cook = Cook.builder()
				.id(1L)
				.recipe(recipe)
				.member(member)
				.createdAt(LocalDateTime.of(2024, 1, 12, 0, 0, 0))
				.build();

		given(cookRepository.save(any()))
				.willReturn(cook);

		// when
		CookCreateDto cookCreateDto = CookCreateDto.builder()
				.recipeId(1L)
				.delete(new ArrayList<>())
				.update(list)
				.build();

		CookInfoDto cookInfoDto = cookService.completeCook(cookCreateDto, "test@gmail.com");

		// then
		assertEquals(1L, cookInfoDto.getId());
		assertEquals(LocalDateTime.of(2024, 1, 12, 0, 0, 0), cookInfoDto.getCreatedAt());
	}

	@Test
	@DisplayName("요리 완료 실패 - 사용자 정보 없음")
	@WithMockMember(role = RoleType.USER)
	void failCompleteCook_UserNotFound() {
		// given
		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.empty());

		CookCreateDto cookCreateDto = CookCreateDto.builder()
				.recipeId(1L)
				.update(null)
				.build();

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> cookService.completeCook(cookCreateDto, "test@gmail.com"));

		// then
		assertEquals(USER_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("요리 완료 실패 - 재료가 존재하지 않음")
	@WithMockMember(role = RoleType.USER)
	void failCompleteCook_IngredientNotFound() {
		// given
		Member member = Member.builder()
				.id(1L)
				.email("test@gmail.com")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		given(fridgeIngredientRepository.findById(1L))
				.willReturn(Optional.empty());

		FridgeIngredientUseDto fridgeIngredientUseDto = FridgeIngredientUseDto.builder()
				.id(1L)
				.memo("양파 2개")
				.build();

		List<FridgeIngredientUseDto> list = new ArrayList<>(
				Collections.singletonList(fridgeIngredientUseDto));

		CookCreateDto cookCreateDto = CookCreateDto.builder()
				.recipeId(1L)
				.delete(new ArrayList<>())
				.update(list)
				.build();

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> cookService.completeCook(cookCreateDto, "test@gmail.com"));

		// then
		assertEquals(INGREDIENT_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("요리 완료 실패 - 레시피 정보 없음")
	@WithMockMember(role = RoleType.USER)
	void failCompleteCook_RecipeNotFound() {
		// given
		Member member = Member.builder()
				.id(1L)
				.email("test@gmail.com")
				.build();

		Fridge fridge = Fridge.builder()
				.id(1L)
				.member(member)
				.build();

		Recipe recipe = Recipe.builder()
				.id(1L)
				.title("양파 볶음")
				.member(member)
				.build();

		FridgeIngredientUseDto fridgeIngredientUseDto = FridgeIngredientUseDto.builder()
				.id(1L)
				.memo("양파 2개")
				.build();

		List<FridgeIngredientUseDto> list = new ArrayList<>(
				Collections.singletonList(fridgeIngredientUseDto));

		CookCreateDto cookCreateDto = CookCreateDto.builder()
				.recipeId(1L)
				.delete(new ArrayList<>())
				.update(list)
				.build();

		FridgeIngredient fridgeIngredient = FridgeIngredient.builder()
				.fridge(fridge)
				.id(1L)
				.name("양파")
				.memo("양파 3개")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		given(fridgeIngredientRepository.findById(anyLong()))
				.willReturn(Optional.of(fridgeIngredient));

		FridgeIngredient updatedFridgeIngredient = FridgeIngredient.builder()
				.id(1L)
				.name("양파")
				.memo("양파 2개")
				.build();

		given(fridgeIngredientRepository.save(any()))
				.willReturn(updatedFridgeIngredient);

		given(recipeRepository.findById(1L))
				.willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> cookService.completeCook(cookCreateDto, "test@gmail.com"));

		// then
		assertEquals(RECIPE_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}
}