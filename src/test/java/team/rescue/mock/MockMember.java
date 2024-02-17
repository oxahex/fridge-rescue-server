package team.rescue.mock;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import team.rescue.auth.type.ProviderType;
import team.rescue.auth.type.RoleType;
import team.rescue.member.entity.Member;

public class MockMember {

	/**
	 * For Test: 새 이메일 회원 가입 유저(이메일 미인증)
	 *
	 * @param nickname 테스트 유저 닉네임
	 * @return 유저
	 */
	protected Member getNewMember(
			String nickname,
			ProviderType provider,
			RoleType role
	) {

		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encodedPassword = passwordEncoder.encode(nickname + nickname + nickname);

		return Member.builder()
				.nickname(nickname)
				.email(nickname + "@gmail.com")
				.password(encodedPassword)
				.emailCode("123456")
				.role(role)
				.provider(provider)
				.build();
	}


	/**
	 * For Test: 저장된 유저
	 *
	 * @param id       테스트 유저 ID
	 * @param nickname 테스트 유저 닉네임
	 * @param provider 가입 방식
	 * @return 저장된 유저
	 */
	protected Member savedMember(
			Long id,
			String nickname,
			ProviderType provider,
			RoleType role
	) {

		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encodedPassword = passwordEncoder.encode(nickname + nickname + nickname);

		return Member.builder()
				.id(id)
				.nickname(nickname)
				.email(nickname + "@gmail.com")
				.password(encodedPassword)
				.role(role)
				.provider(provider)
				.build();
	}
}
