package team.rescue.fridge.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team.rescue.fridge.entity.Fridge;
import team.rescue.member.entity.Member;

@Repository
public interface FridgeRepository extends JpaRepository<Fridge, Long> {
	Optional<Fridge> findByMember(Member member);

	void deleteByMember(Member member);

}
