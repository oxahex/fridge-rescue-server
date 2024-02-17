package team.rescue.recipe.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import team.rescue.recipe.entity.Recipe;
import team.rescue.recipe.entity.RecipeIngredient;

@Repository
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {

  List<RecipeIngredient> findByRecipe(Recipe recipe);

  @Query("SELECT DISTINCT ri.name FROM RecipeIngredient ri WHERE ri.name LIKE CONCAT(:prefix, '%') ORDER BY ri.name ASC")
  List<String> findDistinctNamesByNameStartingWithCustom(String prefix);

}
