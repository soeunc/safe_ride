package com.example.safe_ride.locationInfo.repo;

import com.example.safe_ride.locationInfo.entity.LocationInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationInfoRepo extends JpaRepository<LocationInfo, Long> {
    List<LocationInfo> findBySido(String sido);
    List<LocationInfo> findAllBySido(String sido);
    List<LocationInfo> findAllBySigungu(String sigungu);

    // 지번주소로 검색
    List<LocationInfo> findAllBySidoAndSigunguAndEupmyundong(String sido, String sigungu, String eupmyundong);
}
