package team.rescue.notification.service;

import static team.rescue.error.type.AuthError.ACCESS_DENIED;
import static team.rescue.error.type.ServiceError.NOTIFICATION_NOT_FOUND;
import static team.rescue.error.type.ServiceError.USER_NOT_FOUND;

import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team.rescue.error.exception.AuthException;
import team.rescue.error.exception.ServiceException;
import team.rescue.member.entity.Member;
import team.rescue.member.repository.MemberRepository;
import team.rescue.notification.dto.NotificationDto.NotificationCheckDto;
import team.rescue.notification.dto.NotificationDto.NotificationInfoDto;
import team.rescue.notification.entity.Notification;
import team.rescue.notification.event.NotificationEvent;
import team.rescue.notification.repository.NotificationRepository;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService {

	private final MemberRepository memberRepository;
	private final NotificationRepository notificationRepository;
	private final RedisMessageService redisMessageService;
	private final SseEmitterService sseEmitterService;

	public Page<NotificationInfoDto> getNotifications(String email, Pageable pageable) {
		Member member = memberRepository.findUserByEmail(email)
				.orElseThrow(() -> new ServiceException(USER_NOT_FOUND));

		Page<Notification> notificationPage = notificationRepository.findByMember(member, pageable);

		return notificationPage.map(NotificationInfoDto::of);
	}

	@Transactional
	public void checkNotifications(NotificationCheckDto notificationCheckDto, String email) {
		Member member = memberRepository.findUserByEmail(email)
				.orElseThrow(() -> new ServiceException(USER_NOT_FOUND));

		for (Long notificationId : notificationCheckDto.getNotificationIds()) {
			Notification notification = notificationRepository.findById(notificationId)
					.orElseThrow(() -> new ServiceException(NOTIFICATION_NOT_FOUND));

			if (!Objects.equals(notification.getMember().getId(), member.getId())) {
				throw new AuthException(ACCESS_DENIED);
			}

			notification.updateCheckedAt(LocalDateTime.now());
			notificationRepository.save(notification);
		}
	}

	@Transactional
	public NotificationInfoDto checkNotification(Long notificationId, String email) {
		Member member = memberRepository.findUserByEmail(email)
				.orElseThrow(() -> new ServiceException(USER_NOT_FOUND));

		Notification notification = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new ServiceException(NOTIFICATION_NOT_FOUND));

		if (!Objects.equals(notification.getMember().getId(), member.getId())) {
			throw new AuthException(ACCESS_DENIED);
		}

		notification.updateCheckedAt(LocalDateTime.now());
		Notification updateNotification = notificationRepository.save(notification);

		return NotificationInfoDto.of(updateNotification);
	}

	public SseEmitter subscribe(String email) {
		SseEmitter sseEmitter = sseEmitterService.createEmitter(email);
		sseEmitterService.send("EventStream Created. [userEmail=" + email + "]", email, sseEmitter);

		redisMessageService.subscribe(email);

		sseEmitter.onTimeout(sseEmitter::complete);
		sseEmitter.onError(e -> sseEmitter.complete());
		sseEmitter.onCompletion(() -> {
			sseEmitterService.deleteEmitter(email);
			redisMessageService.removeSubscribe(email);
		});

		return sseEmitter;
	}

	@Transactional
	public void sendNotification(NotificationEvent event) {

		log.info("sendNotification 메서드 수행");

		Member member = memberRepository.findUserByEmail(event.email())
				.orElseThrow(() -> new ServiceException(USER_NOT_FOUND));

		Notification notification = Notification.builder()
				.member(member)
				.notificationType(event.notificationType())
				.notificationProperty(event.notificationProperty())
				.createdAt(event.createdAt())
				.checkedAt(event.checkedAt())
				.build();

		if (!notificationRepository.existsByMemberAndNotificationTypeAndNotificationProperty(
				member,
				notification.getNotificationType(),
				notification.getNotificationProperty()
		)) {

			notificationRepository.save(notification);

			redisMessageService.publish(event.email(), NotificationInfoDto.of(notification));
		}
	}
}
