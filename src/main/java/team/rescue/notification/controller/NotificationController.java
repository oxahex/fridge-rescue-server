package team.rescue.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team.rescue.auth.user.PrincipalDetails;
import team.rescue.common.dto.ResponseDto;
import team.rescue.notification.dto.NotificationDto.NotificationCheckDto;
import team.rescue.notification.dto.NotificationDto.NotificationInfoDto;
import team.rescue.notification.service.NotificationService;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	@PreAuthorize("hasAuthority('USER')")
	public ResponseEntity<SseEmitter> subscribe(
			@AuthenticationPrincipal PrincipalDetails principalDetails
	) {
		return ResponseEntity.ok(
				notificationService.subscribe(principalDetails.getUsername()));
	}

	@GetMapping
	@PreAuthorize("hasAuthority('USER')")
	public ResponseEntity<ResponseDto<Page<NotificationInfoDto>>> getNotifications(
			@AuthenticationPrincipal PrincipalDetails principalDetails,
			@PageableDefault Pageable pageable
	) {
		Page<NotificationInfoDto> notifications = notificationService.getNotifications(
				principalDetails.getUsername(), pageable);

		return ResponseEntity.ok(new ResponseDto<>("알림 조회 성공", notifications));
	}

	@PatchMapping
	@PreAuthorize("hasAuthority('USER')")
	public ResponseEntity<ResponseDto<?>> checkNotifications(
			@RequestBody NotificationCheckDto notificationCheckDto,
			@AuthenticationPrincipal PrincipalDetails principalDetails
	) {

		notificationService.checkNotifications(notificationCheckDto, principalDetails.getUsername());

		return ResponseEntity.ok(new ResponseDto<>("알림 일괄 처리에 성공하였습니다.", null));
	}

	@GetMapping("/{notificationId}")
	@PreAuthorize("hasAuthority('USER')")
	public ResponseEntity<ResponseDto<NotificationInfoDto>> checkNotification(
			@PathVariable Long notificationId,
			@AuthenticationPrincipal PrincipalDetails principalDetails
	) {
		NotificationInfoDto notificationInfoDto = notificationService.checkNotification(notificationId,
				principalDetails.getUsername());

		return ResponseEntity.ok(new ResponseDto<>("알림 확인 처리에 성공하였습니다.", notificationInfoDto));
	}
}
