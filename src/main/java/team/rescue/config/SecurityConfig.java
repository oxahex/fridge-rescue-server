package team.rescue.config;

import static org.springframework.security.config.Customizer.withDefaults;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import team.rescue.auth.filter.JwtAuthenticationFilter;
import team.rescue.auth.filter.JwtAuthorizationFilter;
import team.rescue.auth.handler.EmailAuthenticationFailureHandler;
import team.rescue.auth.handler.EmailAuthorizationFailureHandler;
import team.rescue.auth.handler.OAuthAuthorizationFailureHandler;
import team.rescue.auth.handler.OAuthAuthorizationSuccessHandler;
import team.rescue.auth.service.AuthService;
import team.rescue.auth.service.OAuthService;
import team.rescue.common.redis.RedisRepository;
import team.rescue.fridge.repository.FridgeIngredientRepository;
import team.rescue.fridge.repository.FridgeRepository;
import team.rescue.member.repository.MemberRepository;
import team.rescue.notification.event.NotificationEventPublisher;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final OAuthService oAuthService;
	private final AuthService authService;
	private final PasswordEncoder passwordEncoder;
	private final RedisRepository redisUtil;
	private final ObjectMapper objectMapper;
	private final MemberRepository memberRepository;
	private final NotificationEventPublisher notificationEventPublisher;
	private final FridgeRepository fridgeRepository;
	private final FridgeIngredientRepository fridgeIngredientRepository;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable).sessionManagement(
						(sessionManagement) -> sessionManagement.sessionCreationPolicy(
								SessionCreationPolicy.STATELESS)).formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.headers(HeadersConfigurer::disable) // iframe 비허용 처리
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))  // CORS
				.logout(withDefaults());

		http.authorizeHttpRequests(request -> {

			request.requestMatchers("/").permitAll();        // for EB Health Check
			request.requestMatchers("/api/**").permitAll();

			request.dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll();
			request.anyRequest().authenticated();
		});

		// Exception Handler 등록
		http.exceptionHandling(exceptionHandler -> {
			exceptionHandler.authenticationEntryPoint(
					new EmailAuthenticationFailureHandler()); // 인증 실패(401)
			exceptionHandler.accessDeniedHandler(
					new EmailAuthorizationFailureHandler()); // 인가(권한) 오류(403)
		});

		// OAuth Handler 등록
		http.oauth2Login(oauth2Login -> oauth2Login.userInfoEndpoint(
						userInfoEndpointConfig -> userInfoEndpointConfig.userService(oAuthService))
				.successHandler(new OAuthAuthorizationSuccessHandler(redisUtil, memberRepository))
				.failureHandler(new OAuthAuthorizationFailureHandler()));

		// Filter 등록
		http.apply(new CustomSecurityFilterManager());

		return http.build();
	}

	CorsConfigurationSource corsConfigurationSource() {

		log.debug("[Bean 등록] CorsConfigurationSource");

		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(List.of("*"));  // 나중에 프론트 출처만 허용해야함
		configuration.setAllowedMethods(List.of("*"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);  //클라이언트가 쿠키나 인증 관련 헤더를 사용하여 요청을 할 수 있도록 허용

		// 모든 주소 요청에 적용
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	AuthenticationManager authenticationManager() {

		log.debug("[Bean 등록] AuthenticationManager");

		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(authService);
		provider.setPasswordEncoder(passwordEncoder);
		return new ProviderManager(provider);
	}

	public class CustomSecurityFilterManager extends
			AbstractHttpConfigurer<CustomSecurityFilterManager, HttpSecurity> {

		@Override
		public void configure(HttpSecurity builder) throws Exception {

			AuthenticationManager authenticationManager = builder.getSharedObject(
					AuthenticationManager.class);

			builder.addFilter(
					new JwtAuthenticationFilter(authenticationManager, objectMapper, redisUtil,
							memberRepository, notificationEventPublisher, fridgeRepository,
							fridgeIngredientRepository));
			builder.addFilter(new JwtAuthorizationFilter(authenticationManager));
			super.configure(builder);
		}
	}
}
