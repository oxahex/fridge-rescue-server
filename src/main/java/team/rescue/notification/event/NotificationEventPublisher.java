package team.rescue.notification.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class NotificationEventPublisher {
	private final ApplicationEventPublisher eventPublisher;

	public void publishEvent(NotificationEvent event) {
		log.info("이벤트 발행");
		eventPublisher.publishEvent(event);
	}
}
