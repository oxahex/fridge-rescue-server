package team.rescue.common.redis;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisRepository {

	private final RedisTemplate<String, Object> redisTemplate;

	public void put(RedisPrefix prefix, String key, Object value) {
		redisTemplate.opsForValue().set(
				prefix.getPrefix() + key,
				value,
				prefix.getExpiredTime(),
				TimeUnit.SECONDS
		);
	}

	public void delete(String key) {
		redisTemplate.delete(key);
	}

	public String get(String key) {

		// key에 해당하는 값이 존재하면 해당 값 반환
		if (isExists(key)) {
			return (String) redisTemplate.opsForValue().get(key);
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
