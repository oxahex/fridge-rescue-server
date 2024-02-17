package team.rescue.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static team.rescue.notification.type.NotificationType.INGREDIENT_EXPIRED;
import static team.rescue.notification.type.NotificationType.RECIPE_RECOMMENDED;

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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import team.rescue.auth.type.RoleType;
import team.rescue.mock.WithMockMember;
import team.rescue.notification.dto.NotificationDto.NotificationCheckDto;
import team.rescue.notification.dto.NotificationDto.NotificationInfoDto;
import team.rescue.notification.entity.NotificationProperty;
import team.rescue.notification.service.NotificationService;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@ActiveProfiles(profiles = "test")
@Transactional
class NotificationControllerTest {

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	MockMvc mockMvc;

	@MockBean
	NotificationService notificationService;

	@Test
	@DisplayName("모든 알림 내역 조회 성공")
	@WithMockMember(role = RoleType.USER)
	void successGetNotifications() throws Exception {

		NotificationInfoDto notificationInfoDto1 = NotificationInfoDto.builder()
				.id(1L)
				.notificationType(INGREDIENT_EXPIRED)
				.notificationProperty(new NotificationProperty(1L, 2L, "내용1"))
				.createdAt(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
				.checkedAt(null)
				.build();
		NotificationInfoDto notificationInfoDto2 = NotificationInfoDto.builder()
				.id(2L)
				.notificationType(RECIPE_RECOMMENDED)
				.notificationProperty(new NotificationProperty(1L, 3L, "내용2"))
				.createdAt(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
				.checkedAt(null)
				.build();

		List<NotificationInfoDto> list = new ArrayList<>(
				Arrays.asList(notificationInfoDto1, notificationInfoDto2));

		// given
		given(notificationService.getNotifications(anyString(), any(Pageable.class)))
				.willReturn(new PageImpl<>(list));

		// when
		// then
		mockMvc.perform(get("/api/notifications"))
				.andExpect(jsonPath("$.data.content[0].id").value(1L))
				.andExpect(
						jsonPath("$.data.content[0].notificationType").value(INGREDIENT_EXPIRED.toString()))
				.andExpect(jsonPath("$.data.content[1].id").value(2L))
				.andExpect(
						jsonPath("$.data.content[1].notificationType").value(RECIPE_RECOMMENDED.toString()))
				.andExpect(status().isOk())
				.andDo(print());
	}

	@Test
	@DisplayName("알림 일괄 확인 처리 성공")
	@WithMockMember(role = RoleType.USER)
	void successCheckNotifications() throws Exception {
		// given
		doNothing().when(notificationService)
				.checkNotifications(any(NotificationCheckDto.class), anyString());

		// when
		// then
		mockMvc.perform(patch("/api/notifications")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								NotificationCheckDto.builder()
										.notificationIds(List.of(1L, 2L))
										.build()
						)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("알림 일괄 처리에 성공하였습니다."))
				.andDo(print());
	}

	@Test
	@DisplayName("특정 알림 조회 성공")
	@WithMockMember(role = RoleType.USER)
	void successCheckNotification() throws Exception{
		// given
		NotificationInfoDto notificationInfoDto = NotificationInfoDto.builder()
				.id(1L)
				.notificationType(INGREDIENT_EXPIRED)
				.notificationProperty(new NotificationProperty(1L, 2L, "내용1"))
				.createdAt(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
				.checkedAt(null)
				.build();

		given(notificationService.checkNotification(anyLong(), anyString()))
				.willReturn(notificationInfoDto);

		// when
		// then
		mockMvc.perform(get("/api/notifications/1"))
				.andExpect(jsonPath("$.data.id").value(1L))
				.andExpect(jsonPath("$.data.notificationType").value(INGREDIENT_EXPIRED.toString()))
				.andExpect(status().isOk())
				.andDo(print());
	}
}