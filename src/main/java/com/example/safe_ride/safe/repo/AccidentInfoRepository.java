package com.example.safe_ride.safe.repo;

import com.example.safe_ride.safe.entity.AccidentInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccidentInfoRepository extends JpaRepository<AccidentInfo, Long> {
    List<AccidentInfo> findByBjDongCode(String BjDongCode);
}
