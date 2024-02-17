package team.rescue.common.schedule.report;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ReportScheduler {

	@PersistenceContext
	private EntityManager entityManager;

	private static final String RECIPE_BLOCKED =
			"UPDATE recipe SET is_blocked = true, modified_at = :modifiedAt WHERE is_blocked = false AND report_count > 10";
	private static final String MODIFIED_AT = "modifiedAt";

	@Transactional
	@Scheduled(cron = "0 0 0 * * *") // 매일 0시에 실행
	public void blockReportedRecipe() {
		Query query = entityManager.createNativeQuery(RECIPE_BLOCKED);
		query.setParameter(MODIFIED_AT, LocalDateTime.now());
		int updatedRows = query.executeUpdate();

		log.info("[Report Recipe] {} recipes blocked.", updatedRows);
	}
}
