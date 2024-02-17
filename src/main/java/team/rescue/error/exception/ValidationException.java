package team.rescue.error.exception;

import java.util.Map;
import lombok.Getter;

/**
 * 클라이언트 요청 값 에러
 */
@Getter
public class ValidationException extends RuntimeException {

	private final Map<String, String> errorMap;

	public ValidationException(String message, Map<String, String> errorMap) {
		super(message);
		this.errorMap = errorMap;
	}
}
