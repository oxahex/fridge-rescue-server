package team.rescue.aop;

import static team.rescue.error.type.ServiceError.LOCK_ACQUISITION_FAIL;
import static team.rescue.error.type.ServiceError.LOCK_ALREADY_ASSIGNED;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import team.rescue.error.exception.ServiceException;

@Component
@RequiredArgsConstructor
@Slf4j
public class LockManager {

	private final RedissonClient redissonClient;

	public void lock(String prefix, String key) {
		RLock lock = redissonClient.getLock(getLockKey(prefix, key));
		log.info("Lock 획득 시도 {}", getLockKey(prefix, key));

		try {
			// 5초 시도, 10초안에 해제
			boolean available = lock.tryLock(5, 10, TimeUnit.SECONDS);

			if (!available) {
				log.error("락 획득 실패!");
				throw new ServiceException(LOCK_ALREADY_ASSIGNED);
			}
		} catch (Exception e) {
			log.error("Redis lock failed", e);
			throw new ServiceException(LOCK_ACQUISITION_FAIL);
		}
	}

	public void unlock(String prefix, String key) {
		log.info("{} lock 해제", getLockKey(prefix, key));
		redissonClient.getLock(getLockKey(prefix, key)).unlock();
	}

	private String getLockKey(String prefix, String recipeId) {
		return prefix + ":" + recipeId;
	}

}
