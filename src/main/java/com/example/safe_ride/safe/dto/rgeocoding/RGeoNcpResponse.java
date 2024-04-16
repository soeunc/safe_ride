package com.example.safe_ride.safe.dto.rgeocoding;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RGeoNcpResponse {
    private Map<String, String> status;
    private List<RGeoResult> results;  // 요청 서비스 이름
}
