package team.rescue.auth.service;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import team.rescue.auth.type.ProviderType;
import team.rescue.auth.type.RoleType;
import team.rescue.auth.user.PrincipalDetails;
import team.rescue.error.exception.ServiceException;
import team.rescue.error.type.ServiceError;
import team.rescue.fridge.service.FridgeService;
import team.rescue.member.entity.Member;
import team.rescue.member.repository.MemberRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService extends DefaultOAuth2UserService {

	private static final String PROVIDER_ID = "sub";
	private static final String NICKNAME = "given_name";
	private static final String NAME = "name";
	private static final String EMAIL = "email";

	private final PasswordEncoder passwordEncoder;
	private final MemberRepository memberRepository;

	private final FridgeService fridgeService;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);
		log.info(oAuth2User.getAttributes().toString());

		ProviderType provider = ProviderType.valueOf(
				userRequest.getClientRegistration().getRegistrationId().toUpperCase()); // google
		String providerId = oAuth2User.getAttribute(PROVIDER_ID);
		String nickname = oAuth2User.getAttribute(NICKNAME);
		String name = oAuth2User.getAttribute(NAME);
		String email = oAuth2User.getAttribute(EMAIL);

		String createdPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
		String encryptedPassword = passwordEncoder.encode(createdPassword);

		Optional<Member> findMember
				= memberRepository.findByProviderAndProviderId(provider, providerId);

		if (findMember.isEmpty()) {
			if (memberRepository.existsByEmail(email)) {
				log.error("가입된 이메일이 이미 존재하니 email 로그인을 시도하세요.");
				throw new ServiceException(ServiceError.EMAIL_ALREADY_EXIST);
			}

			Member savedMember = memberRepository.save(Member.builder()
					.email(email)
					.nickname(nickname)
					.password(encryptedPassword)
					.role(RoleType.USER)
					.provider(provider)
					.providerId(providerId)
					.build());

			fridgeService.createFridge(savedMember);

			return new PrincipalDetails(savedMember, oAuth2User.getAttributes());
		} else {
			return new PrincipalDetails(findMember.get(), oAuth2User.getAttributes());
		}
	}
}
