package team.rescue.error.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import team.rescue.error.type.ServiceError;

@Getter
public class ServiceException extends RuntimeException {

	HttpStatus statusCode;
	String errorMessage;

	public ServiceException(ServiceError serviceError) {
		this.statusCode = serviceError.getHttpStatus();
		this.errorMessage = serviceError.getErrorMessage();
	}
}
