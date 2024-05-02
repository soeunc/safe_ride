package com.example.safe_ride.member.repo;

import com.example.safe_ride.member.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BadgeRepo extends JpaRepository<Badge, Long> {
    Optional<Badge> findByMemberId(Long memberId);
}
