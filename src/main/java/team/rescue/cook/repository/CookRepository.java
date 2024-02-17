package team.rescue.cook.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team.rescue.cook.entity.Cook;
import team.rescue.member.entity.Member;

@Repository
public interface CookRepository extends JpaRepository<Cook, Long> {

	Page<Cook> findByMember(Member member, Pageable pageable);
}
