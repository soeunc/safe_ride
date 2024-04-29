package com.example.safe_ride.locationInfo.repo;

import com.example.safe_ride.locationInfo.entity.TempCombinedInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TempCombinedInfoRepo extends JpaRepository<TempCombinedInfo, Long> {
    TempCombinedInfo findByRntstnId(String rntstnId);
    Page<TempCombinedInfo> findAll(Pageable pageable);
}
