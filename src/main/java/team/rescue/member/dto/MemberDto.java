package team.rescue.member.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import team.rescue.auth.type.RoleType;
import team.rescue.member.entity.Member;

public class MemberDto {

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class MemberInfoDto {

		private Long id;
		private String nickname;
		private RoleType role;

		public static MemberInfoDto of(Member member) {
			MemberInfoDto memberInfo = new MemberInfoDto();
			memberInfo.setId(member.getId());
			memberInfo.setNickname(member.getNickname());
			memberInfo.setRole(member.getRole());

			return memberInfo;
		}

		public static MemberInfoDto of(Long memberId, String nickName, RoleType Role) {
			MemberInfoDto memberInfo = new MemberInfoDto();
			memberInfo.setId(memberId);
			memberInfo.setNickname(nickName);
			memberInfo.setRole(Role);

			return memberInfo;
		}
	}

	@Getter
	@Setter
	@Builder
	public static class MemberInfoWithTokenDto {

		private Long id;
		private String nickname;
		private RoleType role;
		private String token;

		public static MemberInfoWithTokenDto of(Member member, String token) {
			return MemberInfoWithTokenDto.builder()
					.id(member.getId())
					.nickname(member.getNickname())
					.role(member.getRole())
					.token(token)
					.build();
		}
	}

	@Getter
	@Setter
	@Builder
	public static class MemberDetailDto {

		private Long id;
		private String nickname;
		private String email;

		public static MemberDetailDto of(Member member) {
			return MemberDetailDto.builder()
					.id(member.getId())
					.nickname(member.getNickname())
					.email(member.getEmail())
					.build();
		}
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class MemberNicknameUpdateDto {

		@NotEmpty(message = "낙네임을 입력해주세요.")
		@Size(max = 15, message = "최대 15자의 이름을 입력해주세요.")
		private String nickname;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class MemberPasswordUpdateDto {

		@NotEmpty(message = "현재 비밀번호를 입력해주세요.")
		private String currentPassword;

		@NotEmpty(message = "변경할 새 비밀번호를 입력해주세요.")
		@Size(max = 20, message = "최대 20글자의 비밀번호를 입력해주세요.")
		private String newPassword;
	}
}
