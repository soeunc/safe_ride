package com.example.safe_ride.myPage.repo;

import com.example.safe_ride.myPage.entity.MyPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface MyPageRepo extends JpaRepository<MyPage, Long> {
    //멤버 id로 데이터 가져오기
    Optional<MyPage> findByMemberId(Long memberId);
    //오늘자(00시00분) 이후 데이터 가져오기
    Optional<MyPage> findByMemberIdAndCreateDateAfter(Long memberId, LocalDateTime today);
}
