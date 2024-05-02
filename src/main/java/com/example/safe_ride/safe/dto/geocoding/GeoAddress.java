package com.example.safe_ride.safe.dto.geocoding;

import lombok.Data;

import java.util.List;

@Data
public class GeoAddress {
    private String roadAddress;  // 도로명 주소
    private String jibunAddress; // 지번 주소
    private String englishAddress; // 영어 주소
    private List<Object> addressElements;  // 주소를 이루는 요소들
    private String x;   // 경도
    private String y;   // 위도
    private Double distance;  // 검색 중심 좌표로부터의 거리
}
