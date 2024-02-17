package team.rescue.recipe.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team.rescue.recipe.entity.Recipe;
import team.rescue.recipe.entity.RecipeStep;

@Repository
public interface RecipeStepRepository extends JpaRepository<RecipeStep, Long> {

  List<RecipeStep> findByRecipe(Recipe recipe);

}
