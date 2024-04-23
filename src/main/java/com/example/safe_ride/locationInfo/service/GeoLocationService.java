package com.example.safe_ride.locationInfo.service;

import com.example.safe_ride.locationInfo.dto.GeoLocationNcpResponse;
import com.example.safe_ride.locationInfo.dto.PointDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoLocationService {
    private final GeoLocationInterface geoLocationInterface;

    //1. 사용자의 IP 얻기
    public String getUserIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For"); // 프록시나 로드 밸런서를 통한 접속을 처리
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP"); // 프록시 서버를 통한 접속 처리
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP"); // 웹로직 서버를 통한 접속 처리
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP"); // 일반 HTTP 클라이언트 IP 처리
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR"); // HTTP 프록시를 통한 접속 처리
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr(); // IP가 헤더에 없을 경우 직접 연결된 클라이언트의 IP 처리
        }
        return ip;
    }

    //2. IP 기반 현재 위치 좌표 받아오기
    public PointDto getPoint(HttpServletRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("ip", getUserIp(request));
        params.put("responseFormatType", "json");
        params.put("ext", "t");

        GeoLocationNcpResponse currentInfo = geoLocationInterface.geoLocation(params);
        if (currentInfo != null && currentInfo.getGeoLocation() != null) {
            log.info(currentInfo.toString());
            return new PointDto(
                    currentInfo.getGeoLocation().getLat(),
                    currentInfo.getGeoLocation().getLng()
            );
        } else {
            log.error("정보를 받아오는데 실패하였습니다.");
            return null;
        }
    }
    //2. 현재 위치 좌표(lag, lot)와 일치하는 station을 필터링하여 파싱


    //3. view에서 사용

}
