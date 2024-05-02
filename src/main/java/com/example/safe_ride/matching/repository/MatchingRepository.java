package com.example.safe_ride.matching.repository;

import com.example.safe_ride.matching.entity.Matching;
import com.example.safe_ride.matching.entity.MatchingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MatchingRepository extends JpaRepository<Matching, Long> {

    Optional<Matching> findFirstByMemberId(Long memberId);

    Page<Matching> findByRegionMetropolitanCity(Pageable pageable, String metropolitanCity);

    Page<Matching> findByRegionMetropolitanCityAndRegionCity(Pageable pageable, String metropolitanCity, String city);

    @Modifying
    @Query("UPDATE Matching m SET m.status = :status WHERE m.id = :matchingId")
    void updateMatchingStatus(@Param("matchingId") Long matchingId, @Param("status") MatchingStatus status);
}