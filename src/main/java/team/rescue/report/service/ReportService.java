package team.rescue.report.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.rescue.aop.DistributedLock;
import team.rescue.error.exception.ServiceException;
import team.rescue.error.type.ServiceError;
import team.rescue.member.entity.Member;
import team.rescue.member.repository.MemberRepository;
import team.rescue.recipe.entity.Recipe;
import team.rescue.recipe.repository.RecipeRepository;
import team.rescue.report.dto.ReportDto.ReportInfoDto;
import team.rescue.report.entity.Report;
import team.rescue.report.repository.ReportRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

	private final MemberRepository memberRepository;
	private final RecipeRepository recipeRepository;
	private final ReportRepository reportRepository;

	/**
	 * 특정 레시피 신고
	 *
	 * @param recipeId   신고할 레시피 아이디
	 * @param reporterId 신고 유저 ID
	 * @param reason     신고 이유
	 * @return 신고 처리 결과
	 */
	@Transactional
	@DistributedLock(prefix = "report_recipe")
	public ReportInfoDto createReport(
			Long recipeId,
			Long reporterId,
			String reason
	) {

		log.info("[리포트 생성] reportedRecipeId={} reporterId={}, reason={}",
				recipeId, reporterId, reason
		);

		// 신고 유저
		Member member = memberRepository.getReferenceById(reporterId);

		// 신고 대상 레시피 신고 카운드 추가
		Recipe recipe = recipeRepository.findById(recipeId)
				.orElseThrow(() -> new ServiceException(ServiceError.RECIPE_NOT_FOUND));

		// 이미 해당 레시피를 신고한 경우
		validateReport(member, recipe);

		// 레시피 신고 횟수 증가 처리
		recipe.increaseReportCount();

		Report report = Report.builder()
				.reason(reason)
				.reportMember(member)
				.reportedRecipe(recipe)
				.build();

		return ReportInfoDto.of(reportRepository.save(report));
	}


	private void validateReport(Member member, Recipe recipe) {
		boolean exist =
				reportRepository.existsByReportMemberAndReportedRecipe(member, recipe);

		if (exist) {
			throw new ServiceException(ServiceError.REPORT_ALREADY_REPORTED);
		}
	}
}
