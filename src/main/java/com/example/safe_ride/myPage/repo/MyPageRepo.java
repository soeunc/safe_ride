package com.example.safe_ride.myPage.repo;

import com.example.safe_ride.myPage.entity.MyPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyPageRepo extends JpaRepository<MyPage, Long> {
}
