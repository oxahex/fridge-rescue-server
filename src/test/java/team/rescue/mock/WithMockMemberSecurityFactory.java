package team.rescue.mock;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import team.rescue.auth.user.PrincipalDetails;
import team.rescue.member.entity.Member;

public class WithMockMemberSecurityFactory implements
		WithSecurityContextFactory<WithMockMember> {

	@Override
	public SecurityContext createSecurityContext(WithMockMember mockCustomUser) {

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		PrincipalDetails principal = new PrincipalDetails(
				Member.builder()
						.email(mockCustomUser.value())
						.role(mockCustomUser.role())
						.build()
		);

		Authentication auth = new UsernamePasswordAuthenticationToken(
				principal, "", principal.getAuthorities()
		);

		context.setAuthentication(auth);
		return context;
	}
}
