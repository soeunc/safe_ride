package com.example.safe_ride.locationInfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BicycleInfoDto {
    private String lcgvmnInstCd;        // 지자체 코드(1100000000)
    private String lcgvmnInstNm;        // 지자체명(서울특별시)
    private String rntstnId;            // 대여소ID(ST-10)
    private String rntstnNm;            // 대여소명(108. 서교동 사거리)
    private String lat;                 // 위도(37.5527458200)
    private String lot;                 // 경도(126.9186172500)
    private String bcyclTpkctNocs;      // 자전거 주차 총 건수(12)
}