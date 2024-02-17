package team.rescue.auth.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import team.rescue.member.entity.Member;

@Getter
public class PrincipalDetails implements UserDetails, OAuth2User {

	private final Member member;
	private Map<String, Object> attributes;

	public PrincipalDetails(Member member) {
		this.member = member;
	}

	public PrincipalDetails(Member member, Map<String, Object> attributes) {
		this.member = member;
		this.attributes = attributes;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(this.member.getRole().name()));
		return authorities;
	}

	@Override
	public String getPassword() {
		return this.member.getPassword();
	}

	@Override
	public String getUsername() {
		return this.member.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return this.member.getIsEnabled();
	}

	@Override
	public Map<String, Object> getAttributes() {
		return this.attributes;
	}

	@Override
	public String getName() {
		return attributes.get("sub").toString();
	}
}
