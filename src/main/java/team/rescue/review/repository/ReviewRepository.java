package team.rescue.review.repository;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import team.rescue.cook.entity.Cook;
import team.rescue.review.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

	List<Review> findByCook(Cook cook);

	@Query("select r from Review r join fetch r.member where r.recipe.id = :recipeId")
	Slice<Review> findReviewsByRecipeId(Long recipeId, PageRequest pageRequest);
}
