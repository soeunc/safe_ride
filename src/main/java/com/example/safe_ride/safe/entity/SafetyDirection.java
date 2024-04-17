package com.example.safe_ride.safe.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter
@Entity
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class SafetyDirection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
//    private String safetyInfor;  // 안전지역 안내
//    private String address;      // 사용자 현재 위치(지번, 도로명)
//    private String sido;
//    private String sigungu;
//    private String eubmyundong;
//    private String addressCode;
    private Double lnt;          // 경도
    private Double lat;          // 위도
}
