package com.example.safe_ride.locationInfo.service;

import com.example.safe_ride.locationInfo.dto.GeoLocationNcpResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import java.util.Map;

public interface GeoLocationInterface {
    @GetExchange
    GeoLocationNcpResponse geoLocation(
            @RequestParam
            Map<String, Object> params
    );
}