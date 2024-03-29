package com.dilly.jwt;

import com.dilly.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	RefreshToken findByMember(Member member);

	boolean existsByMember(Member member);
}
