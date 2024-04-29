package com.example.safe_ride.matching.entity;

import com.example.safe_ride.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Manner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rated_member_id")
    private Member ratedMember; // 평가 받는 회원

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rater_member_id")
    private Member raterMember; // 평가하는 회원

    private int score; // 매너 점수
    private String comment; // 추가적인 코멘트

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id")
    private Matching matching; // 해당 매칭 정보
}