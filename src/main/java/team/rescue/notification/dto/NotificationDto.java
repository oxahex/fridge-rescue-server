package team.rescue.notification.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import team.rescue.notification.entity.Notification;
import team.rescue.notification.entity.NotificationProperty;
import team.rescue.notification.type.NotificationType;

public class NotificationDto {

	@Getter
	@Setter
	@Builder
	public static class NotificationInfoDto {

		private Long id;
		private NotificationType notificationType;
		private NotificationProperty notificationProperty;
		@JsonSerialize(using = LocalDateTimeSerializer.class)
		@JsonDeserialize(using = LocalDateTimeDeserializer.class)
		private LocalDateTime createdAt;
		@JsonSerialize(using = LocalDateTimeSerializer.class)
		@JsonDeserialize(using = LocalDateTimeDeserializer.class)
		private LocalDateTime checkedAt;

		public static NotificationInfoDto of(Notification notification) {
			return NotificationInfoDto.builder()
					.id(notification.getId())
					.notificationType(notification.getNotificationType())
					.notificationProperty(notification.getNotificationProperty())
					.createdAt(notification.getCreatedAt())
					.checkedAt(notification.getCheckedAt())
					.build();
		}

	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class NotificationCheckDto {

		private List<Long> notificationIds;
	}

}
