package com.example.safe_ride.safe.service;

import com.example.safe_ride.safe.dto.NaviWithQueryDto;
import com.example.safe_ride.safe.dto.PointDto;
import com.example.safe_ride.safe.dto.geocoding.GeoNcpResponse;
import com.example.safe_ride.safe.dto.rgeocoding.RGeoNcpResponse;
import com.example.safe_ride.safe.dto.rgeocoding.RGeoRegion;
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
        // 중심좌표(위도, 경도)를 기준으로 주소 검색
//        params.put("coordinate", dto.getStart().toQueryValue());
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
//                + " " +
//                code.getType() + " " +
//                code.getMappingId();
        return new RGeoResponseDto(bjDongCode.trim());
    }

    // 사용자 주소로 법정동 코드 추출
    // TODO 현재 locateAddress, getBjDongCode 메서드가 두번씩 실행되고 있다.(다른 방법 생각필요)
    public RGeoResponseDto getBjDongCodeFromAddress(NaviWithQueryDto dto) {
        // 주소를 좌표로 변환
        PointDto pointDto = locateAddress(dto);
        log.info("좌표 확인: {}", pointDto);
        if (pointDto == null) {
            // 좌표 변환에 실패한 경우
            log.error("주소로부터 좌표를 얻는 데 실패했습니다.");
            return null;
        }

        log.info("법정동 코드 확인: {}", getBjDongCode(pointDto));

        // 변환된 좌표를 사용하여 법정동 코드를 얻음
        return getBjDongCode(pointDto);
    }

}
