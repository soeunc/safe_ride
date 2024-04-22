package com.example.safe_ride.safe.service;

import com.example.safe_ride.safe.dto.geocoding.GeoNcpResponse;
import com.example.safe_ride.safe.dto.rgeocoding.RGeoNcpResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import java.util.Map;

// http 요청 보내는 클래스
public interface NcpMapApiService {
    // 구현체를 만들어서 http 요청을 보내는 프록시 객체를 만들어 줘야 사용가능하다.

    // Geocode
    @GetExchange("/map-geocode/v2/geocode")
    GeoNcpResponse geocode(
            @RequestParam
            Map<String, Object> params
    );

    // revers Geocode
    @GetExchange("/map-reversegeocode/v2/gc")
    RGeoNcpResponse reversGeocode(
            @RequestParam
            Map<String, Object> params
    );
}
