package team.rescue.common.redis;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisRepository {

	private final RedisTemplate<String, Object> redisTemplate;
	private static final String REDIS_KEY_SEPARATOR = "_";

	public void put(RedisPrefix prefix, String key, Object value, Long expirationTime) {
		if (expirationTime != null) {
			redisTemplate.opsForValue().set(
					prefix.name() + REDIS_KEY_SEPARATOR + key, value, expirationTime, TimeUnit.SECONDS
			);
		} else {
			redisTemplate.opsForValue().set(key, value);
		}
	}

	public void delete(String key) {
		redisTemplate.delete(key);
	}

	public Object get(String key) {

		// key에 해당하는 값이 존재하면 해당 값 반환
		if (isExists(key)) {
			return redisTemplate.opsForValue().get(key);
		} else {
			return null;
		}
	}

	public boolean isExists(String key) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(key));
	}

	public void setExpireTime(String key, long expirationTime) {
		redisTemplate.expire(key, expirationTime, TimeUnit.SECONDS);
	}

}
