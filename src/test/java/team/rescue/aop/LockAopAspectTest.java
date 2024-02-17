package team.rescue.aop;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.rescue.error.exception.ServiceException;
import team.rescue.error.type.ServiceError;

@ExtendWith(MockitoExtension.class)
class LockAopAspectTest {

	@Mock
	LockManager lockManager;

	@Mock
	ProceedingJoinPoint proceedingJoinPoint;

	@Mock
	MethodSignature methodSignature;

	@Mock
	DistributedLock distributedLock;

	@InjectMocks
	LockAopAspect lockAopAspect;

	@Test
	@DisplayName("락 획득 및 해제 성공")
	void lockAndUnlock() throws Throwable {
		// given

		given(proceedingJoinPoint.getSignature())
				.willReturn(methodSignature);

		given(methodSignature.getMethod())
				.willReturn(getClass().getMethod("exampleMethod"));

		lockAopAspect.distributeLock(proceedingJoinPoint, 123L);

		// then
		verify(lockManager, times(1)).lock(eq("recipe_bookmark"), anyString());
		verify(proceedingJoinPoint, times(1)).proceed();
		verify(lockManager, times(1)).unlock(eq("recipe_bookmark"), anyString());
	}

	@Test
	@DisplayName("예외가 발생해도 락 획득 및 해제 성공")
	void lockAndUnlock_evenIfException() throws Throwable {
		// given
		given(proceedingJoinPoint.getSignature())
				.willReturn(methodSignature);

		given(methodSignature.getMethod())
				.willReturn(getClass().getMethod("exampleMethod"));

		given(proceedingJoinPoint.proceed())
				.willThrow(new ServiceException(ServiceError.LOCK_ACQUISITION_FAIL));

		Assertions.assertThrows(ServiceException.class,
				() -> lockAopAspect.distributeLock(proceedingJoinPoint, 123L));

		// then
		verify(lockManager, times(1)).lock(eq("recipe_bookmark"), anyString());
		verify(proceedingJoinPoint, times(1)).proceed();
		verify(lockManager, times(1)).unlock(eq("recipe_bookmark"), anyString());

	}

	@DistributedLock(prefix = "recipe_bookmark")
	public String exampleMethod() {
		return "result";
	}
}