package com.example.safe_ride.safe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CoordinateDto {
    private String spotNm;       // 사고 발생 위치 명칭
    private String accidentLng;  // 경도
    private String accidentLat;  // 위도
    private String occrrncCnt;   // 발생 건수
    private String dthDnvCnt;    // 사망자 수
}
