package team.rescue.auth.dto;

import lombok.Getter;
import lombok.Setter;
import team.rescue.auth.type.RoleType;
import team.rescue.member.entity.Member;

public class LoginDto {

	@Getter
	@Setter
	public static class LoginReqDto {

		private String email;
		private String password;
	}

	@Getter
	public static class LoginResDto {

		private final Long id;
		private final String nickname;
		private final RoleType roleType;
		private final String token;

		public LoginResDto(Member user, String token) {
			this.id = user.getId();
			this.nickname = user.getNickname();
			this.roleType = user.getRole();
			this.token = token;
		}
	}
}
