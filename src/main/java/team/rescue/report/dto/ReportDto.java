package team.rescue.report.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import team.rescue.member.dto.MemberDto.MemberInfoDto;
import team.rescue.recipe.dto.RecipeDto.RecipeInfoDto;
import team.rescue.report.entity.Report;

public class ReportDto {

	@Getter
	@Setter
	public static class ReportCreateDto {

		@NotEmpty(message = "신고 대상 레시피 아이디는 필수값입니다.")
		private Long recipeId;

		@NotEmpty(message = "신고 이유를 입력해주세요.")
		@Size(max = 200, message = "200자 미만으로 입력해주세요.")
		private String reason;
	}

	@Getter
	@Setter
	public static class ReportInfoDto {

		private Long id;
		private String reason;
		private LocalDateTime createdAt;
		private MemberInfoDto reporter;
		private RecipeInfoDto recipe;

		public static ReportInfoDto of(Report report) {

			ReportInfoDto reportInfo = new ReportInfoDto();
			reportInfo.setId(report.getId());
			reportInfo.setReason(report.getReason());
			reportInfo.setCreatedAt(report.getCreatedAt());
			reportInfo.setReporter(MemberInfoDto.of(report.getReportMember()));
			reportInfo.setRecipe(RecipeInfoDto.of(report.getReportedRecipe()));

			return reportInfo;
		}
	}

}
