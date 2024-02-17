package team.rescue.search.service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import team.rescue.recipe.repository.RecipeIngredientRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {


  private final RedisTemplate<String, List<String>> listRedisTemplate;
  private final RecipeIngredientRepository recipeIngredientRepository;

  public List<String> getIngredient(String keyword) {
    String redisKey = "ingredient:" + keyword;
    log.debug("키워드 = {}", keyword);

    // redis에서 key값을 확인
    List<String> ingredientList = listRedisTemplate.opsForValue().get(redisKey);

    if (ingredientList != null) {
      log.debug("ingredientList : {}", ingredientList);
      return ingredientList;
    }

    log.debug("ingredientList는 비어있음 : {}", ingredientList);

    // 없다면 db 체크 후 redis에 저장
    List<String> ingredientsFromDb =
        recipeIngredientRepository.findDistinctNamesByNameStartingWithCustom(keyword);

    log.debug("ingredientsFromDb : {}", ingredientsFromDb);

    // db에 없다면 redis에 저장하지 않도록
    if (ingredientsFromDb != null && !ingredientsFromDb.isEmpty()) {

      List<String> topIngredients =
          (ingredientsFromDb.size() > 10) ? ingredientsFromDb.subList(0, 10) : ingredientsFromDb;

      // Redis에 데이터 저장
      listRedisTemplate.opsForValue().set(redisKey, topIngredients, Duration.ofDays(1));

      return topIngredients;
    }

    // db에서도 아무것도 못찾았으면 빈 리스트 반환
    return Collections.emptyList();
  }
}
