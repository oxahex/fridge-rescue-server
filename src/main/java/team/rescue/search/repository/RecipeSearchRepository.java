package team.rescue.search.repository;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Component;
import team.rescue.search.entity.RecipeDoc;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecipeSearchRepository {

	private final ElasticsearchOperations searchOperations;

	/**
	 * Recipe Document 저장
	 *
	 * @param recipeDoc 저장할 Recipe Document
	 * @return 저장한 Recipe Document
	 */
	public RecipeDoc save(RecipeDoc recipeDoc) {
		return searchOperations.save(recipeDoc);
	}

	/**
	 * Recipe Document 재료 기반 검색
	 *
	 * @param ingredients 검색할 재료 문자열
	 * @param pageable    페이지네이션 정보
	 * @return 재료와 매치되는 Recipe Documents
	 */
	public SearchPage<RecipeDoc> searchByIngredients(String ingredients, Pageable pageable) {
		Criteria criteria = new Criteria();

		for (String ingredient : ingredients.split(" ")) {
			criteria = criteria.or(Criteria.where("ingredients").is(ingredient));
		}

		Query query = new CriteriaQuery(criteria).setPageable(pageable);
		SearchHits<RecipeDoc> searchHits = searchOperations.search(query, RecipeDoc.class);

		return SearchHitSupport.searchPageFor(searchHits, pageable);
	}

	/**
	 * Recipe Document 키워드 기반 검색
	 *
	 * @param keyword  검색할 keyword
	 * @param pageable 페이지네이션 정보
	 * @return 키워드와 매치되는 Recipe Documents
	 */
	public SearchPage<RecipeDoc> searchByKeyword(String keyword, Pageable pageable) {
		Criteria criteriaForTitle = Criteria.where("title").matches(keyword);
		Criteria criteriaForSummary = Criteria.where("summary").matches(keyword);
		Criteria combinedCriteria = criteriaForTitle.or(criteriaForSummary);

		Query query = new CriteriaQuery(combinedCriteria).setPageable(pageable);
		SearchHits<RecipeDoc> searchHits = searchOperations.search(query, RecipeDoc.class);
		return SearchHitSupport.searchPageFor(searchHits, pageable);
	}
}
