package com.example.safe_ride.matching.entity;

import jakarta.persistence.*;
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
public class Manner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long matchingArticled; // 매칭 신청 아이디
    private Long score;            // 평가 점수
    private String comment;        // 리뷰 코멘트
    private Long reviewId;         // 리뷰한 사람 Id
    private Long reviewerId;       // 리뷰 받은 사람 Id

}
