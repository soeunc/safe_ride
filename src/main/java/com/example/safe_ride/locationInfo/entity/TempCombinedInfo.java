package com.example.safe_ride.locationInfo.entity;

import com.example.safe_ride.locationInfo.dto.CombinedInfoDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TempCombinedInfo {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    private String mngInstNm;                // 관리기관명(서울특별시)
    private String bcyclDataCrtrYmd;         // 데이터기준일자(2024-02-08)
    private String lcgvmnInstCd;             // 지자체 코드(1100000000)
    private String lcgvmnInstNm;             // 지자체명(서울특별시)
    private String rntstnId;                 // 대여소 아이디(ST-10)
    private String rntstnNm;                 // 자전거대여소명(108. 서교동 사거리)
    private String roadNmAddr;               // 소재지도로명주소(서울특별시 마포구 양화로 93 427)
    private String lat;                      // 위도(37.5527458200)
    private String lot;                      // 경도(126.9186172500)
    private String operBgngHrCn;             // 운영시작시각(00:00)
    private String operEndHrCn;              // 운영종료시각(23:59)
    private String rpfactInstlYn;            // 수리대설치여부(N)
    private String arinjcInstlYn;            // 공기주입기비치여부(N)
    private String arinjcTypeNm;             // 공기주입기유형(FV)
    private String rntstnFcltTypeNm;         // 자전거대여소구분(무인)
    private String rntstnOperDayoffDayCn;    // 휴무일(연중무휴)
    private String rntFeeTypeNm;             // 요금구분(유료)
    private String mngInstTelno;             // 관리기관전화번호(https://data.seoul.go.kr)
    private int bcyclTpkctNocs;          // 자전거 주차 총 건수(12)
    private Double distance;                // 검색 위치와의 거리
}
