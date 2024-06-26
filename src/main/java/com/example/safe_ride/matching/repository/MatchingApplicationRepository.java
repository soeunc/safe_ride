package com.example.safe_ride.matching.repository;

import com.example.safe_ride.matching.entity.MatchingApplication;
import com.example.safe_ride.matching.entity.MatchingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchingApplicationRepository extends JpaRepository<MatchingApplication, Long> {
    // 매칭 ID에 해당하는 모든 매칭 신청 검색
    List<MatchingApplication> findAllByMatching_Id(Long matchingId);

    MatchingApplication findByMatching_IdAndApplicant_Id(Long matchingId, Long applicantId);

    List<MatchingApplication> findAllByMatching_IdAndStatus(Long matchingId, MatchingStatus status);


    Optional<MatchingApplication> findByMatchingId(Long matchingId);
}
