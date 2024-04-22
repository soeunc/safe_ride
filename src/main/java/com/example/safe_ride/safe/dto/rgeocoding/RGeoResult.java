package com.example.safe_ride.safe.dto.rgeocoding;

import lombok.Data;

import java.util.Map;

@Data
public class RGeoResult {
    private String name; // 변환 작업 이름
    private RGoCode code;
    private RGeoRegion region; // 지역 명칭 정보
}
