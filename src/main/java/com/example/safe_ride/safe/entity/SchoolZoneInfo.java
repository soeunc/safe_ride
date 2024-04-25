package com.example.safe_ride.safe.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolZoneInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String bjDongCode;  // 법정동 코드
    private String sidoSggNnm;  // 지점의 시도시군구명
    private String spotNm; // 사고가 발생한 시도 및 시군구 명칭
    private Integer occrrncCnt; // 발생 건수
    private Integer casltCnt; // 사상자 수
    private Integer dthDnvCnt; // 사망자 수
    private Integer seDnvCnt; // 중상자 수
    private Integer slDnvCnt; // 경상자 수
    private String loCrd; // 경도
    private String laCrd; // 위도
}
