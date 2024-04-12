package com.example.safe_ride.matching.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;

@Getter
@Entity
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class Matching {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long memberId;
    private Long applicantId;      // 매칭글 신청자
    private String city;           // 지역
    private String comment;        // 매칭 코멘트
    private Timestamp createTime;  // 작성 시간
    private String status;         // 승인 상태
}
