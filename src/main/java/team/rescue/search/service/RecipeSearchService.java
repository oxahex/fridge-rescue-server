package team.rescue.search.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.stereotype.Service;
import team.rescue.error.exception.ServiceException;
import team.rescue.error.type.ServiceError;
import team.rescue.fridge.entity.Fridge;
import team.rescue.fridge.entity.FridgeIngredient;
import team.rescue.fridge.repository.FridgeRepository;
import team.rescue.member.entity.Member;
import team.rescue.member.repository.MemberRepository;
import team.rescue.recipe.dto.RecipeDto.RecipeInfoDto;
import team.rescue.search.entity.RecipeDoc;
import team.rescue.search.repository.RecipeSearchRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeSearchService {

	private final RecipeSearchRepository recipeSearchRepository;
	private final MemberRepository memberRepository;
	private final FridgeRepository fridgeRepository;

	public Page<RecipeInfoDto> searchRecipeByKeyword(
			String keyword, Pageable pageable
	) {

		log.info("키워드 검색 서비스");

		SearchPage<RecipeDoc> searchHits =
				recipeSearchRepository.searchByKeyword(keyword, pageable);

		// 검색 결과가 비어 있는지 확인
		if (searchHits.isEmpty()) {
			throw new ServiceException(ServiceError.SEARCH_KEYWORD_NOT_FOUND);
		}

		List<RecipeInfoDto> recipeInfoDtos = searchHits.getContent().stream()
				.map(hit -> RecipeInfoDto.of(hit.getContent()))
				.collect(Collectors.toList());

		return new PageImpl<>(recipeInfoDtos);
	}

	public Page<RecipeInfoDto> searchRecipeByFridge(
			Long memberId, Pageable pageable
	) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new ServiceException(ServiceError.USER_NOT_FOUND));

		// 멤버 아이디로 해당 멤버의 냉장고 조회
		Fridge fridge = fridgeRepository.findByMember(member)
				.orElseThrow(() -> new ServiceException(ServiceError.FRIDGE_NOT_FOUND));

		// 재료를 담을 StringBuilder
		StringBuilder ingredients = new StringBuilder();

		// 냉장고 재료를 StringBuilder에 추가
		for (FridgeIngredient fridgeIngredient : fridge.getIngredientList()) {
			ingredients.append(fridgeIngredient.getName()).append(" ");
		}

		// 문자열 검색
		SearchPage<RecipeDoc> searchPage =
				recipeSearchRepository.searchByIngredients(ingredients.toString().strip(), pageable);

		List<RecipeInfoDto> recipeInfoDtoList = searchPage.getSearchHits().stream()
				.map(recipeDocSearchHit -> RecipeInfoDto.of(recipeDocSearchHit.getContent()))
        .collect(Collectors.toList());

		return new PageImpl<>(recipeInfoDtoList);
	}

}
