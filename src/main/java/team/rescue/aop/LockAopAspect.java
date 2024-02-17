package team.rescue.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Component
@Aspect
@RequiredArgsConstructor
@Slf4j
public class LockAopAspect {

	private final LockManager lockManager;

	@Around("@annotation(DistributedLock) && args(recipeId, ..)")
	public Object distributeLock(ProceedingJoinPoint pjp, Long recipeId) throws Throwable {

		MethodSignature signature = (MethodSignature) pjp.getSignature();
		DistributedLock lock = signature.getMethod().getAnnotation(DistributedLock.class);

		lockManager.lock(lock.prefix(), String.valueOf(recipeId));

		try {
			return pjp.proceed();
		} finally {
			lockManager.unlock(lock.prefix(), String.valueOf(recipeId));
		}
	}
}
