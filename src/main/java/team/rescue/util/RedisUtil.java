package team.rescue.util;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// TODO: util은 static class만 위치하므로 이후 패키지 이동 필요
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RedisUtil {

	private final RedisTemplate<String, Object> redisTemplate;

	@Transactional
	public void put(String key, Object value, Long expirationTime) {
		if (expirationTime != null) {
			redisTemplate.opsForValue().set(key, value, expirationTime, TimeUnit.SECONDS);
		} else {
			redisTemplate.opsForValue().set(key, value);
		}
	}

	@Transactional
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
