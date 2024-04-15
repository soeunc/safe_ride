package com.example.safe_ride.safe.service;

import com.example.safe_ride.safe.dto.PointDto;
import com.example.safe_ride.safe.dto.SafetyDirectionDto;
import com.example.safe_ride.safe.dto.geocoding.GeoNcpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SafetyService {
    private final NcpMapApiService mapApiService;

    // grocode를 사용해서 사용자 현재 위치를 입력 받아서 위치를 반환하는 메서드
    public PointDto locateAddress(SafetyDirectionDto dto) {
        // 주소의 좌표 찾기
        Map<String, Object> params = new HashMap<>();
        params.put("query", dto.getAddress());
        params.put("page", 1);
        params.put("count", 1);
        GeoNcpResponse response = mapApiService.geocode(params);
        log.info(response.toString());

        if (response.getAddresses().isEmpty()) {
            log.error("주소 검색 결과가 없습니다.");
            return null; // 주소 검색 결과가 없을 경우
        }

        Double lat = Double.valueOf(response.getAddresses().get(0).getY());
        Double lng = Double.valueOf(response.getAddresses().get(0).getX());

        // 좌표 정보 반환
        return new PointDto(lat, lng);
    }

}
