package team.rescue.notification.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import team.rescue.notification.service.NotificationService;

@RequiredArgsConstructor
@Component
public class NotificationEventHandler {

	private final NotificationService notificationService;

	@EventListener
	public void handleEvent(NotificationEvent event) {
		notificationService.sendNotification(event);
	}

}
