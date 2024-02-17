package team.rescue.cook.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import team.rescue.auth.type.RoleType;
import team.rescue.cook.dto.CookDto.CookCreateDto;
import team.rescue.cook.dto.CookDto.CookInfoDto;
import team.rescue.cook.service.CookService;
import team.rescue.fridge.dto.FridgeIngredientDto.FridgeIngredientUseDto;
import team.rescue.mock.WithMockMember;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@ActiveProfiles(profiles = "test")
@Transactional
class CookControllerTest {

	@MockBean
	CookService cookService;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	MockMvc mockMvc;

	@Test
	@DisplayName("요리 완료 성공")
	@WithMockMember(role = RoleType.USER)
	void successCompleteCook() throws Exception {
		// given
		given(cookService.completeCook(any(), anyString()))
				.willReturn(CookInfoDto.builder()
						.id(1L)
						.createdAt(LocalDateTime.of(2024, 1, 12, 0, 0, 0))
						.build());

		FridgeIngredientUseDto fridgeIngredientUseDto1 = FridgeIngredientUseDto.builder()
				.id(1L)
				.memo("양파 2개")
				.build();

		FridgeIngredientUseDto fridgeIngredientUseDto2 = FridgeIngredientUseDto.builder()
				.id(2L)
				.memo("당근 반개")
				.build();

		List<Long> deleteList = new ArrayList<>(1);

		List<FridgeIngredientUseDto> list = new ArrayList<>(
				Arrays.asList(fridgeIngredientUseDto1, fridgeIngredientUseDto2));

		// when
		// then
		mockMvc.perform(post("/api/cooks")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								CookCreateDto.builder()
										.recipeId(1L)
										.delete(deleteList)
										.update(list)
										.build()
						)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(1L))
				.andExpect(jsonPath("$.data.createdAt").value("2024-01-12T00:00:00"))
				.andDo(print());
	}
}