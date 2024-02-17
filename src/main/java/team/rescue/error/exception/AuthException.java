package team.rescue.error.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import team.rescue.error.type.AuthError;

/**
 * Spring Security 내 예외 처리
 */
@Getter
public class AuthException extends InternalAuthenticationServiceException {

	HttpStatus httpStatus;
	String errorMessage;

	public AuthException(AuthError authError) {
		super(authError.getErrorMessage());
		this.httpStatus = authError.getHttpStatus();
		this.errorMessage = authError.getErrorMessage();
	}
}
