package com.example.safe_ride.article.repository;

import com.example.safe_ride.article.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
    // 모든 광역자치구 가져오기
    @Query("SELECT DISTINCT r.metropolitanCity FROM Region r")
    List<String> findAllMetropolitanCities();

    // 선택된 광역자치구에 해당하는 시군구 가져오기
    List<Region> findByMetropolitanCity(String metropolitanCity);

    // 광역자치구와 도시에 해당하는 Region 조회
    Optional<Region> findByMetropolitanCityAndCity(String metropolitanCity, String city);
}