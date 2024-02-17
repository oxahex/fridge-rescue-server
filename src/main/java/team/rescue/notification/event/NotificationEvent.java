package team.rescue.notification.event;

import java.time.LocalDateTime;
import lombok.Builder;
import team.rescue.notification.entity.NotificationProperty;
import team.rescue.notification.type.NotificationType;

@Builder
public record NotificationEvent(
		String email,
		NotificationType notificationType,
		NotificationProperty notificationProperty,
		LocalDateTime createdAt,
		LocalDateTime checkedAt
) {

}
