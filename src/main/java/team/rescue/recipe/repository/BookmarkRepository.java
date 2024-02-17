package team.rescue.recipe.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team.rescue.member.entity.Member;
import team.rescue.recipe.entity.Bookmark;
import team.rescue.recipe.entity.Recipe;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

	Page<Bookmark> findByMember(Member member, Pageable pageable);

	Optional<Bookmark> findByRecipeAndMember(Recipe recipe, Member member);

	boolean existsByRecipeAndMember(Recipe recipe, Member member);

	void deleteByRecipeAndMember(Recipe recipe, Member member);
}
