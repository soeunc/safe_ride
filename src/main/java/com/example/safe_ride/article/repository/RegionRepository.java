package com.example.safe_ride.article.repository;

import com.example.safe_ride.article.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
    // 모든 광역자치구 가져오기
    @Query("SELECT DISTINCT r.metropolitanCity FROM Region r")
    List<String> findAllMetropolitanCities();


    // 광역자치구에 해당하는 모든 도시 가져오기
    @Query("SELECT DISTINCT r.city FROM Region r WHERE r.metropolitanCity = :metropolitanCity")
    List<String> findCitiesByMetropolitanCity(@Param("metropolitanCity") String metropolitanCity);

    List<Region> findByMetropolitanCity(String metropolitanCity);
    Optional<Region> findByMetropolitanCityAndCity(String metropolitanCity, String city);
}