package team.rescue.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static team.rescue.error.type.AuthError.ACCESS_DENIED;
import static team.rescue.error.type.ServiceError.NOTIFICATION_NOT_FOUND;
import static team.rescue.error.type.ServiceError.USER_NOT_FOUND;
import static team.rescue.notification.type.NotificationType.INGREDIENT_EXPIRED;
import static team.rescue.notification.type.NotificationType.RECIPE_RECOMMENDED;

import java.time.LocalDateTime;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import team.rescue.auth.type.RoleType;
import team.rescue.error.exception.AuthException;
import team.rescue.error.exception.ServiceException;
import team.rescue.member.entity.Member;
import team.rescue.member.repository.MemberRepository;
import team.rescue.mock.WithMockMember;
import team.rescue.notification.dto.NotificationDto.NotificationCheckDto;
import team.rescue.notification.dto.NotificationDto.NotificationInfoDto;
import team.rescue.notification.entity.Notification;
import team.rescue.notification.entity.NotificationProperty;
import team.rescue.notification.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@Mock
	MemberRepository memberRepository;

	@Mock
	NotificationRepository notificationRepository;

	@InjectMocks
	NotificationService notificationService;

	@Test
	@DisplayName("모든 알림 조회 성공")
	@WithMockMember(role = RoleType.USER)
	void successGetNotifications() {
		// given
		Member member = Member.builder()
				.id(1L)
				.email("test@gmail.com")
				.build();

		Notification notification1 = Notification.builder()
				.id(1L)
				.notificationType(INGREDIENT_EXPIRED)
				.notificationProperty(new NotificationProperty(1L, 2L, "내용1"))
				.createdAt(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
				.checkedAt(null)
				.build();
		Notification notification2 = Notification.builder()
				.id(2L)
				.notificationType(RECIPE_RECOMMENDED)
				.notificationProperty(new NotificationProperty(1L, 3L, "내용2"))
				.createdAt(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
				.checkedAt(null)
				.build();

		List<Notification> list = new ArrayList<>(
				Arrays.asList(notification1, notification2));

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		given(notificationRepository.findByMember(any(), any()))
				.willReturn(new PageImpl<>(list));

		// when
		Page<NotificationInfoDto> notificationInfoDtoPage = notificationService.getNotifications(
				"test@gmail.com",
				any(Pageable.class));

		// then
		assertEquals(1L, notificationInfoDtoPage.getContent().get(0).getId());
		assertEquals(2L, notificationInfoDtoPage.getContent().get(1).getId());
		assertEquals(INGREDIENT_EXPIRED,
				notificationInfoDtoPage.getContent().get(0).getNotificationType());
		assertEquals(RECIPE_RECOMMENDED,
				notificationInfoDtoPage.getContent().get(1).getNotificationType());
	}

	@Test
	@DisplayName("모든 알림 내역 조회 실패 - 사용자 정보 없음")
	@WithMockMember(role = RoleType.USER)
	void failGetNotifications_UserNotFound() {
		// given
		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> notificationService.getNotifications("test@gmail.com", PageRequest.of(0, 2)));

		// then
		assertEquals(USER_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("알림 일괄 확인 처리 성공")
	@WithMockMember(role = RoleType.USER)
	void successCheckNotifications() {
		// given
		Member member = Member.builder()
				.id(1L)
				.email("test@gmail.com")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		Notification notification = Notification.builder()
				.member(member)
				.id(1L)
				.notificationType(INGREDIENT_EXPIRED)
				.build();

		given(notificationRepository.findById(anyLong()))
				.willReturn(Optional.of(notification));

		NotificationCheckDto notificationCheckDto = NotificationCheckDto.builder()
				.notificationIds(List.of(1L))
				.build();

		// when
		notificationService.checkNotifications(notificationCheckDto, "test@gmail.com");

		// then
		verify(notificationRepository, times(1)).save(any());
	}

	@Test
	@DisplayName("알림 일괄 확인 처리 실패 - 사용자 정보 없음")
	@WithMockMember(role = RoleType.USER)
	void failCheckNotifications_UserNotFound() {
		// given
		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.empty());

		NotificationCheckDto notificationCheckDto = NotificationCheckDto.builder()
				.notificationIds(List.of(1L))
				.build();

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> notificationService.checkNotifications(notificationCheckDto, "test@gmail.com"));

		// then
		assertEquals(USER_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("알림 일괄 확인 처리 실패 - 알림 정보 없음")
	void failCheckNotifications_NotificationNotFound() {
		// given
		Member member = Member.builder()
				.id(1L)
				.email("test@gmail.com")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		given(notificationRepository.findById(anyLong()))
				.willReturn(Optional.empty());

		NotificationCheckDto notificationCheckDto = NotificationCheckDto.builder()
				.notificationIds(List.of(1L))
				.build();

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> notificationService.checkNotifications(notificationCheckDto, "test@gmail.com"));

		// then
		assertEquals(NOTIFICATION_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("알림 일괄 확인 처리 실패 - 권한 없음")
	void failCheckNotifications_AccessDenied() {
		// given
		Member member = Member.builder()
				.id(1L)
				.email("test@gmail.com")
				.build();

		Member member2 = Member.builder()
				.id(2L)
				.email("test2@gmail.com")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		Notification notification = Notification.builder()
				.id(1L)
				.member(member2)
				.notificationType(INGREDIENT_EXPIRED)
				.build();

		given(notificationRepository.findById(anyLong()))
				.willReturn(Optional.of(notification));

		NotificationCheckDto notificationCheckDto = NotificationCheckDto.builder()
				.notificationIds(List.of(1L))
				.build();

		// when
		AuthException authException = assertThrows(AuthException.class,
				() -> notificationService.checkNotifications(notificationCheckDto, "test@gmail.com"));

		// then
		assertEquals(ACCESS_DENIED.getHttpStatus(), authException.getHttpStatus());
	}

	@Test
	@DisplayName("특정 알림 조회 성공")
	@WithMockMember(role = RoleType.USER)
	void successCheckNotification() {
		// given
		Member member = Member.builder()
				.id(1L)
				.email("test@gmail.com")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		Notification notification = Notification.builder()
				.member(member)
				.id(1L)
				.notificationType(INGREDIENT_EXPIRED)
				.checkedAt(null)
				.build();

		given(notificationRepository.findById(anyLong()))
				.willReturn(Optional.of(notification));

		given(notificationRepository.save(any()))
				.willReturn(Notification.builder()
						.id(1L)
						.checkedAt(LocalDateTime.now())
						.build());

		// when
		NotificationInfoDto notificationInfoDto = notificationService.checkNotification(1L,
				"test@gmail.com");

		// then
		assertEquals(1L, notificationInfoDto.getId());
		assertNotNull(notificationInfoDto.getCheckedAt());
	}

	@Test
	@DisplayName("특정 알림 조회 실패 - 사용자 정보 없음")
	@WithMockMember(role = RoleType.USER)
	void failCheckNotification_UserNotFound() {
		// given
		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> notificationService.checkNotification(1L, "test@gmail.com"));

		// then
		assertEquals(USER_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("특정 알림 조회 실패 - 알림 정보 없음")
	@WithMockMember(role = RoleType.USER)
	void failCheckNotification_NotificationNotFound() {
		// given
		Member member = Member.builder()
				.id(1L)
				.email("test@gmail.com")
				.build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		given(notificationRepository.findById(anyLong()))
				.willReturn(Optional.empty());

		// when
		ServiceException serviceException = assertThrows(ServiceException.class,
				() -> notificationService.checkNotification(1L, "test@gmail.com"));

		// then
		assertEquals(NOTIFICATION_NOT_FOUND.getHttpStatus(), serviceException.getStatusCode());
	}

	@Test
	@DisplayName("특정 알림 조회 실패 - 권한 없음")
	@WithMockMember(role = RoleType.USER)
	void failCheckNotification_AccessDenied() {
		// given
		Member member = Member.builder()
				.id(1L)
				.email("test@gmail.com")
				.build();

		Member member2 = Member.builder()
				.id(2L).build();

		given(memberRepository.findUserByEmail("test@gmail.com"))
				.willReturn(Optional.of(member));

		Notification notification = Notification.builder()
				.member(member2)
				.id(1L)
				.notificationType(INGREDIENT_EXPIRED)
				.checkedAt(null)
				.build();

		given(notificationRepository.findById(anyLong()))
				.willReturn(Optional.of(notification));

		// when
		AuthException authException = assertThrows(AuthException.class,
				() -> notificationService.checkNotification(1L, "test@gmail.com"));

		// then
		assertEquals(ACCESS_DENIED.getHttpStatus(), authException.getHttpStatus());
	}
}
