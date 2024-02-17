package team.rescue.auth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Slf4j
public class OAuthAuthorizationFailureHandler implements AuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException exception
	) throws IOException, ServletException {

		log.error("OAuth2 인증 실패 : {}", exception.getMessage());

		if (exception instanceof OAuth2AuthenticationException oauth2Exception) {

			if ("access_denied".equals(oauth2Exception.getError().getErrorCode())) {
				// 사용자가 동의를 거부한 경우
				log.warn("사용자가 동의를 거부했습니다.");
				// 사용자에게 적절한 안내 메시지 전달
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "사용자가 동의를 거부했습니다.");
				return;
			}

			if ("invalid_client".equals(oauth2Exception.getError().getErrorCode())) {
				// 잘못된 클라이언트 정보
				log.error("잘못된 클라이언트 정보입니다.");
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "잘못된 클라이언트 정보입니다.");
				return;
			}

			if ("invalid_request".equals(oauth2Exception.getError().getErrorCode())) {
				// 리디렉션 URI 불일치 등의 잘못된 요청
				log.error("잘못된 요청입니다.");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 요청입니다.");
				return;
			}
		}

		// 그 외의 경우에 대한 처리
		log.error("알 수 없는 오류가 발생했습니다.");
		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다.");

	}
}
