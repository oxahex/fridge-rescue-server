package team.rescue.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import team.rescue.common.dto.ResponseDto;
import team.rescue.error.exception.AuthException;
import team.rescue.error.exception.ServiceException;
import team.rescue.error.exception.ValidationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


	/**
	 * 인증 관련 에러 핸들링
	 *
	 * @param e AuthException
	 * @return Error Response with custom AuthException Status Code
	 */
	@ExceptionHandler(AuthException.class)
	public ResponseEntity<?> authException(AuthException e) {

		log.error(e.getErrorMessage());

		ResponseDto<?> response = ResponseDto.builder()
				.message(e.getErrorMessage())
				.data(null).build();

		return new ResponseEntity<>(response, e.getHttpStatus());
	}

	/**
	 * 서비스(비즈니스) 로직 관련 에러 핸들링
	 *
	 * @param e ServiceException
	 * @return Error Response with custom ServiceException Status Code
	 */
	@ExceptionHandler(ServiceException.class)
	public ResponseEntity<ResponseDto<Object>> serviceException(ServiceException e) {

		log.error(e.getErrorMessage());

		ResponseDto<Object> response = new ResponseDto<>(e.getErrorMessage(), null);

		return new ResponseEntity<>(response, e.getStatusCode());
	}


	/**
	 * 유효성 검사 관련 에러 핸들링
	 *
	 * @param e ValidationException
	 * @return Error Response with BAD_REQUEST(400)
	 */
	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ResponseDto<Object>> validationException(ValidationException e) {

		log.error(e.getMessage());

		ResponseDto<Object> response = new ResponseDto<>(e.getMessage(), e.getErrorMap());

		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	/**
	 * 서버 에러 핸들링
	 *
	 * @param e ServiceException
	 * @return Error Response with custom RuntimeException Status Code(500)
	 */
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ResponseDto<Object>> runtimeException(RuntimeException e) {

		log.error(e.getMessage());

		ResponseDto<Object> response = new ResponseDto<>(e.getMessage(), null);

		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
