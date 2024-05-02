package com.example.safe_ride.safe.service;

import com.example.safe_ride.safe.dto.NaviWithQueryDto;
import com.example.safe_ride.safe.dto.PointDto;
import com.example.safe_ride.safe.dto.geocoding.GeoNcpResponse;
import com.example.safe_ride.safe.dto.rgeocoding.RGeoNcpResponse;
import com.example.safe_ride.safe.dto.rgeocoding.RGeoResponseDto;
import com.example.safe_ride.safe.dto.rgeocoding.RGoCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NcpService {
    private final NcpMapApiService mapApiService;

    // grocode를 사용해서 사용자 현재 위치를 입력 받아서 좌표를 반환하는 메서드
    public PointDto locateAddress(NaviWithQueryDto dto) {
        // 주소의 좌표 찾기
        Map<String, Object> params = new HashMap<>();
        params.put("query", dto.getQuery());
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
        return new PointDto(lng, lat);
    }

    // reverse grocode를 사용해서 좌표를 입력받아 법정동 코드를 반환하는 메서드
    public RGeoResponseDto getBjDongCode(PointDto point) {
        Map<String, Object> params = new HashMap<>();
        params.put("coords", point.toQueryValue());
        params.put("orders", "legalcode"); // 법정동으로 변환 작업
        params.put("output", "json");
        RGeoNcpResponse response = mapApiService.reversGeocode(params);
        RGoCode code = response.getResults()
                .get(0)
                .getCode();

        String bjDongCode = code.getId();
        return new RGeoResponseDto(bjDongCode.trim());
    }

}
