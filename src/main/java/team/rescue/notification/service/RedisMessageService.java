package team.rescue.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import team.rescue.notification.RedisSubscriber;
import team.rescue.notification.dto.NotificationDto.NotificationInfoDto;

@RequiredArgsConstructor
@Service
public class RedisMessageService {

	private static final String CHANNEL_PREFIX = "channel:";

	private final RedisMessageListenerContainer container;
	private final RedisSubscriber subscriber;
	private final RedisTemplate<String, Object> jsonRedisTemplate;

	public void subscribe(String channel) {
		container.addMessageListener(subscriber, ChannelTopic.of(getChannelName(channel)));
	}

	public void publish(String channel, NotificationInfoDto notificationInfoDto) {
		jsonRedisTemplate.convertAndSend(getChannelName(channel), notificationInfoDto);
	}

	public void removeSubscribe(String channel) {
		container.removeMessageListener(subscriber, ChannelTopic.of(getChannelName(channel)));
	}

	private String getChannelName(String id) {
		return CHANNEL_PREFIX + id;
	}

}
