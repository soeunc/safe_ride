package com.example.safe_ride.matching.entity;

import com.example.safe_ride.member.entity.Member;
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
    @OneToOne
    private Member member;          // 매칭글 작성자(리뷰 하는 사람)
    private Long matchingArticleId; // 매칭 신청한 아이디(리뷰 받은 사람)
    private int score;             // 평가 점수
    private String comment;        // 리뷰 코멘트
}