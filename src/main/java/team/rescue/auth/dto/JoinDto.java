package team.rescue.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import team.rescue.auth.type.RoleType;
import team.rescue.member.entity.Member;

public class JoinDto {

	@Getter
	@Setter
	public static class JoinReqDto {

		@NotEmpty(message = "사용할 닉네임을 입력해주세요.")
		@Size(max = 15, message = "최대 15자의 닉네임을 입력해주세요.")
		private String nickname;

		// 최대 30자
		@NotEmpty(message = "로그인에 사용할 이메일을 입력해주세요.")
		@Size(max = 50, message = "50자 미만의 이메일을 입력해주세요.")
		private String email;

		// 8 ~ 20
		@NotEmpty(message = "로그인에 사용할 비밀번호를 입력해주세요.")
		@Size(max = 20, message = "최대 20글자의 비밀번호를 입력해주세요.")
		private String password;
	}

	@Getter
	@Setter
	public static class EmailConfirmDto {

		private String email;

		@NotEmpty(message = "이메일 인증 코드를 입력해주세요.")
		@Size(min = 6, max = 6, message = "올바른 인증 코드가 아닙니다.")
		private String code;
	}


	@Getter
	@Setter
	public static class JoinResDto {

		private Long id;
		private String nickname;
		private String email;
		private RoleType role;
		private LocalDateTime createdAt;

		public JoinResDto(Member member) {

			this.id = member.getId();
			this.nickname = member.getNickname();
			this.email = member.getEmail();
			this.role = member.getRole();
			this.createdAt = member.getCreatedAt();
		}
	}
}
