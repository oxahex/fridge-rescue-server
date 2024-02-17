package team.rescue.notification.service;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team.rescue.notification.dto.NotificationDto.NotificationInfoDto;
import team.rescue.notification.repository.SseEmitterRepository;

@RequiredArgsConstructor
@Service
@Slf4j
public class SseEmitterService {

	private static final Long EMITTER_TIMEOUT = 60L * 1000 * 60;

	private final SseEmitterRepository sseEmitterRepository;

	public SseEmitter createEmitter(String id) {
		return sseEmitterRepository.save(id, new SseEmitter(EMITTER_TIMEOUT));
	}

	public void deleteEmitter(String id) {
		sseEmitterRepository.deleteById(id);
	}

	public void sendNotificationToClient(String email, NotificationInfoDto notificationInfoDto) {
		sseEmitterRepository.findById(email)
				.ifPresent(sseEmitter -> send(notificationInfoDto, email, sseEmitter));
	}

	public void send(Object data, String id, SseEmitter sseEmitter) {
		try {
			log.info("send to client {} : [{}]", id, data);

			sseEmitter.send(SseEmitter.event()
					.id(id)
					.data(data, MediaType.APPLICATION_JSON));
		} catch (IOException e) {
			log.error("알림 전송 예외 발생");
			sseEmitterRepository.deleteById(id);
		}
	}
}
