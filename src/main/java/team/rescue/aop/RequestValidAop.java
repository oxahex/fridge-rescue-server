package team.rescue.aop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import team.rescue.error.exception.ValidationException;
import team.rescue.validator.ListValidator;

@Slf4j
@Component
@Aspect
@RequiredArgsConstructor
public class RequestValidAop {

	private final ListValidator listValidator;

	@Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
	public void postMapping() {
	}

	@Pointcut("execution(* team.rescue..controller.*.*(..))")
	public void pointCut() {
	}

	/**
	 * Request Body 데이터 유효성 체크
	 * <p>POST, PUT Request 전후 핸들링
	 */
	@Around("pointCut() || @annotation(ListValidation)")
	public Object requestValidationAdvice(ProceedingJoinPoint pjp) throws Throwable {

		Object[] args = pjp.getArgs();
		for (Object arg : args) {

			if (arg instanceof List<?> list) {
				BindingResult bindingResult = new BeanPropertyBindingResult(list, "list");
				listValidator.validate(list, bindingResult);

				if (bindingResult.hasErrors()) {
					Map<String, String> errorMap = new HashMap<>();

					for (FieldError fieldError : bindingResult.getFieldErrors()) {
						log.info("[유효성 리스트 실패] field={}, message={}", fieldError.getField(),
								fieldError.getDefaultMessage());
						errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
					}

					throw new ValidationException("유효성 검사 실패", errorMap);
				}
			}

			if (arg instanceof BindingResult bindingResult) {

				if (bindingResult.hasErrors()) {
					Map<String, String> errorMap = new HashMap<>();

					for (FieldError error : bindingResult.getFieldErrors()) {
						log.info("[유효성 실패] field={}, message={}", error.getField(), error.getDefaultMessage());
						errorMap.put(error.getField(), error.getDefaultMessage());
					}

					// Validation Exception
					throw new ValidationException("유효성 검사 실패", errorMap);
				}
			}
		}

		return pjp.proceed();
	}
}
