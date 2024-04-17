package com.example.safe_ride.safe.dto.rgeocoding;

import lombok.Data;

@Data
public class RGeoRegionDetails {
    private String name; // 국가 코드 최상위 도메인 두 자리
    private Object coords; // 행정 구역과 관련된 좌표
}

