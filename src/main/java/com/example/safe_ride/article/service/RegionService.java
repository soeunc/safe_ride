package com.example.safe_ride.article.service;

import com.example.safe_ride.article.entity.Region;
import com.example.safe_ride.article.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegionService {
    private final RegionRepository regionRepository;

    // 모든 광역자치구 가져오기
    public List<String> getAllMetropolitanCities() {
        return regionRepository.findAllMetropolitanCities();
    }

    // 선택된 광역자치구에 해당하는 시군구 가져오기
    public List<Region> getCitiesByMetropolitanCity(String metropolitanCity) {
        return regionRepository.findByMetropolitanCity(metropolitanCity);
    }

    // 광역자치구와 도시에 해당하는 Region ID 가져오기
    public Long getRegionIdByMetropolitanCityAndCity(String metropolitanCity, String city) {
        return regionRepository.findByMetropolitanCityAndCity(metropolitanCity, city)
                .map(Region::getId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid metropolitan city or city"));
    }
}