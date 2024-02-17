package team.rescue.search.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.rescue.auth.user.PrincipalDetails;
import team.rescue.common.dto.ResponseDto;
import team.rescue.recipe.dto.RecipeDto.RecipeInfoDto;
import team.rescue.search.service.RecipeSearchService;
import team.rescue.search.service.SearchService;
import team.rescue.search.type.SortType;

@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

	private final SearchService searchService;
	private final RecipeSearchService recipeSearchService;

	@GetMapping("/recipe/keyword")
	@PreAuthorize("permitAll()")
	public ResponseEntity<ResponseDto<Page<RecipeInfoDto>>> searchRecipesByKeyword(
			@RequestParam String keyword,
			Pageable pageable,
			@RequestParam SortType sortType
	) {

		log.info("[레시피 키워드 검색] keyword={}", keyword);

		Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
				Sort.by(sortType.getDirection(), sortType.getSortBy()));

		Page<RecipeInfoDto> recipeDocs =
				recipeSearchService.searchRecipeByKeyword(keyword, sortedPageable);

		return new ResponseEntity<>(
				new ResponseDto<>(keyword + "로 검색한 레시피 목록입니다.", recipeDocs),
				HttpStatus.OK
		);
	}

	@GetMapping("/recipe/fridge")
	@PreAuthorize("hasAuthority('USER')")
	@Transactional(readOnly = true)
	public ResponseEntity<ResponseDto<Page<RecipeInfoDto>>> searchRecipesByFridge(
			Pageable pageable,
			@AuthenticationPrincipal PrincipalDetails details
	) {

		log.info("[레시피 유저 재료 기반 검색] memberId={}", details.getMember().getId());

		Page<RecipeInfoDto> recipeInfoPage =
				recipeSearchService.searchRecipeByFridge(details.getMember().getId(), pageable);

		return new ResponseEntity<>(
				new ResponseDto<>("냉장고 재료로 검색한 레시피 목록입니다.", recipeInfoPage),
				HttpStatus.OK
		);
	}

	/**
	 * 재료 자동완성
	 *
	 * @param keyword 키워드
	 * @return 키워드로 시작하는 재료 목록
	 */
	@GetMapping("/ingredient")
	public ResponseEntity<List<String>> getIngredient(
			@RequestParam String keyword
	) {

		List<String> recommendedIngredients = searchService.getIngredient(keyword);

		return new ResponseEntity<>(recommendedIngredients, HttpStatus.OK);
	}

}
