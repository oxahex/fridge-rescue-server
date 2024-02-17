package team.rescue.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.rescue.member.entity.Member;
import team.rescue.recipe.entity.Recipe;
import team.rescue.report.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {

	boolean existsByReportMemberAndReportedRecipe(Member member, Recipe recipe);

}
