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

    public List<Region> getCitiesByMetropolitanCity(String metropolitanCity) {
        return regionRepository.findByMetropolitanCity(metropolitanCity);
    }

    public Long getRegionIdByMetropolitanCityAndCity(String metropolitanCity, String city) {
        return regionRepository.findByMetropolitanCityAndCity(metropolitanCity, city)
                .map(Region::getId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid metropolitan city or city"));
    }

    public List<String> getAllMetropolitanCities() {
        return regionRepository.findAllMetropolitanCities();
    }

}