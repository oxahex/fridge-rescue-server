package team.rescue.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.ObjectUtils;
import team.rescue.auth.provider.JwtTokenProvider;
import team.rescue.auth.user.PrincipalDetails;

@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

	public static final String TOKEN_HEADER = "Authorization";
	public static final String TOKEN_PREFIX = "Bearer ";

	public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {

		log.debug("[{}] JWT 유효성 검사", request.getRequestURI());

		// Access Token Request Header에  없는 경우 인가 처리 하지 않고 다음 필터로
		String header = request.getHeader(TOKEN_HEADER);

		if (!(header != null && header.startsWith(TOKEN_PREFIX))) {
			log.debug("[JwtAuthorizationFilter] 토큰이 없음");
			filterChain.doFilter(request, response);
			return;
		}

		// Access token from request header(Authorization)
		String accessToken = (!ObjectUtils.isEmpty(header) && header.startsWith(TOKEN_PREFIX)) ?
				header.substring(TOKEN_PREFIX.length()) : null;

		// Expired access token
		if (JwtTokenProvider.isExpiredToken(accessToken)) {

			log.debug("[Expired Access Token] start!");

			// 응답 상태 코드와 컨텐트 타입 설정
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);

			// 응답 바디 데이터 구성
			Map<String, Object> body = new HashMap<>();
//      body.put("code", AuthError.EXPIRED_ACCESS_TOKEN.getHttpStatus());
//      body.put("error", AuthError.EXPIRED_ACCESS_TOKEN.getErrorMessage());
			body.put("code", "401 Unauthorized");
			body.put("error", "만료된 토큰");

			// JSON으로 변환하여 응답 스트림에 작성
			new ObjectMapper().writeValue(response.getOutputStream(), body);

			// 다음 필터를 타지 않음
			return;
		}

		PrincipalDetails principalDetails = JwtTokenProvider.verify(accessToken);

		// 생성된 AuthUser 객체로 인증된 Authentication 객체 생성
		Authentication authentication =
				new UsernamePasswordAuthenticationToken(
						principalDetails, null, principalDetails.getAuthorities()
				);

		// SecurityContextHolder 저장
		SecurityContextHolder.getContext().setAuthentication(authentication);

		filterChain.doFilter(request, response);
	}
}

