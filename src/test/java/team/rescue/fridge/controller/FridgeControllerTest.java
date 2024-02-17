package team.rescue.fridge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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
import team.rescue.fridge.dto.FridgeDto;
import team.rescue.fridge.dto.FridgeIngredientDto.FridgeIngredientCreateDto;
import team.rescue.fridge.dto.FridgeIngredientDto.FridgeIngredientInfoDto;
import team.rescue.fridge.dto.FridgeIngredientDto.FridgeIngredientUpdateDto;
import team.rescue.fridge.service.FridgeService;
import team.rescue.mock.WithMockMember;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@ActiveProfiles(profiles = "test")
@Transactional
class FridgeControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	FridgeService fridgeService;

	List<FridgeIngredientInfoDto> fridgeIngredientInfoDtoList = new ArrayList<>();

	@BeforeEach
	public void setFridgeIngredientInfoDtoList() {
		FridgeIngredientInfoDto fridgeIngredientInfoDto1 = FridgeIngredientInfoDto.builder()
				.id(1L)
				.name("양파")
				.memo("하얀 양파")
				.expiredAt(LocalDate.of(2024, 1, 30))
				.build();
		FridgeIngredientInfoDto fridgeIngredientInfoDto2 = FridgeIngredientInfoDto.builder()
				.id(2L)
				.name("오이")
				.memo("초록 오이")
				.expiredAt(LocalDate.of(2024, 1, 30))
				.build();
		FridgeIngredientInfoDto fridgeIngredientInfoDto3 = FridgeIngredientInfoDto.builder()
				.id(3L)
				.name("당근")
				.memo("주황 당근")
				.expiredAt(LocalDate.of(2024, 1, 30))
				.build();

		fridgeIngredientInfoDtoList.add(fridgeIngredientInfoDto1);
		fridgeIngredientInfoDtoList.add(fridgeIngredientInfoDto2);
		fridgeIngredientInfoDtoList.add(fridgeIngredientInfoDto3);
	}


	@Test
	@DisplayName("냉장고 조회 성공")
	@WithMockMember(role = RoleType.USER)
	void successGetFridge() throws Exception {
		// given

		given(fridgeService.getFridge(anyString()))
				.willReturn(FridgeDto.builder()
						.id(1L)
						.fridgeIngredientInfoList(fridgeIngredientInfoDtoList)
						.build());

		// when
		// then
		mockMvc.perform(get("/api/fridge"))
				.andExpect(jsonPath("$.data.id").value(1L))
				.andExpect(jsonPath("$.data.fridgeIngredientInfoList[0].id").value(1L))
				.andExpect(jsonPath("$.data.fridgeIngredientInfoList[1].id").value(2L))
				.andExpect(jsonPath("$.data.fridgeIngredientInfoList[2].id").value(3L))
				.andExpect(jsonPath("$.data.fridgeIngredientInfoList[0].name").value("양파"))
				.andExpect(jsonPath("$.data.fridgeIngredientInfoList[1].name").value("오이"))
				.andExpect(jsonPath("$.data.fridgeIngredientInfoList[2].name").value("당근"))
				.andExpect(status().isOk())
				.andDo(print());
	}

	@Test
	@DisplayName("냉장고 재료 등록 성공")
	@WithMockMember(role = RoleType.USER)
	void successAddIngredient() throws Exception {
		// given
		FridgeIngredientCreateDto fridgeIngredientCreateDto1 = FridgeIngredientCreateDto.builder()
				.name("양파")
				.memo("하얀 양파")
				.expiredAt(LocalDate.of(2024, 1, 30))
				.build();

		FridgeIngredientCreateDto fridgeIngredientCreateDto2 = FridgeIngredientCreateDto.builder()
				.name("오이")
				.memo("초록 오이")
				.expiredAt(LocalDate.of(2024, 1, 30))
				.build();
		FridgeIngredientCreateDto fridgeIngredientCreateDto3 = FridgeIngredientCreateDto.builder()
				.name("당근")
				.memo("주황 당근")
				.expiredAt(LocalDate.of(2024, 1, 30))
				.build();

		List<FridgeIngredientCreateDto> fridgeIngredientCreateDtoList = new ArrayList<>(
				Arrays.asList(fridgeIngredientCreateDto1, fridgeIngredientCreateDto2,
						fridgeIngredientCreateDto3));

		given(fridgeService.addIngredient(anyString(), anyList()))
				.willReturn(fridgeIngredientInfoDtoList);

		// when
		// then
		mockMvc.perform(post("/api/fridge/ingredients")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								fridgeIngredientCreateDtoList
						)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].name").value("양파"))
				.andExpect(jsonPath("$.data[1].name").value("오이"))
				.andExpect(jsonPath("$.data[2].name").value("당근"))
				.andExpect(jsonPath("$.data[0].memo").value("하얀 양파"))
				.andExpect(jsonPath("$.data[1].memo").value("초록 오이"))
				.andExpect(jsonPath("$.data[2].memo").value("주황 당근"))
				.andDo(print());
	}

	@Test
	@DisplayName("냉장고 재료 수정 및 삭제 성공")
	@WithMockMember(role = RoleType.USER)
	void successUpdateIngredient() throws Exception {
		// given
		FridgeIngredientInfoDto fridgeIngredientInfoDto2 = FridgeIngredientInfoDto.builder()
				.id(2L)
				.name("오이")
				.memo("초록 오이")
				.expiredAt(LocalDate.of(2024, 1, 30))
				.build();
		FridgeIngredientInfoDto fridgeIngredientInfoDto3 = FridgeIngredientInfoDto.builder()
				.id(3L)
				.name("당근")
				.memo("주황 당근")
				.expiredAt(LocalDate.of(2024, 1, 30))
				.build();

		List<FridgeIngredientInfoDto> result = new ArrayList<>(
				Arrays.asList(fridgeIngredientInfoDto2, fridgeIngredientInfoDto3));

		given(fridgeService.updateIngredient(anyString(), any(FridgeIngredientUpdateDto.class)))
				.willReturn(result);

		// when
		// then
		mockMvc.perform(put("/api/fridge/ingredients")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								FridgeIngredientUpdateDto.builder()
										.deleteItem(List.of(1L))
										.updateItem(result)
										.build()
						)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].name").value("오이"))
				.andExpect(jsonPath("$.data[1].name").value("당근"))
				.andDo(print());
	}

}