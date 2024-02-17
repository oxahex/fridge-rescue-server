package team.rescue.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import team.rescue.notification.dto.NotificationDto.NotificationInfoDto;
import team.rescue.notification.service.SseEmitterService;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisSubscriber implements MessageListener {

	private static final String CHANNEL_PREFIX = "channel:";

	private final ObjectMapper objectMapper;
	private final SseEmitterService sseEmitterService;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {

			log.info("RedisSubscriber onMessage");
			String channel = new String(message.getChannel()).substring(CHANNEL_PREFIX.length());

			NotificationInfoDto notificationInfoDto = objectMapper.readValue(message.getBody(),
					NotificationInfoDto.class);

			sseEmitterService.sendNotificationToClient(channel, notificationInfoDto);
		} catch (IOException e) {
			log.error("IOException 발생", e);
		}
	}
}
