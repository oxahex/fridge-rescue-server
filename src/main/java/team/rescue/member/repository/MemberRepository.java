package team.rescue.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team.rescue.auth.type.ProviderType;
import team.rescue.member.entity.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

	boolean existsByEmail(String email);

	Optional<Member> findUserByEmail(String email);

	Optional<Member> findByProviderAndProviderId(ProviderType provider, String providerId);
}
