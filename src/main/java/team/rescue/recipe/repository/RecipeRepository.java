package team.rescue.recipe.repository;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team.rescue.member.entity.Member;
import team.rescue.recipe.entity.Recipe;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

	Page<Recipe> findByMember(Member member, Pageable pageable);

	Page<Recipe> findAllByOrderByCreatedAtDesc(Pageable pageable);

	Page<Recipe> findAllByOrderByBookmarkCountDesc(Pageable pageable);

	Integer countByCreatedAtBefore(LocalDateTime createdAt);
}
