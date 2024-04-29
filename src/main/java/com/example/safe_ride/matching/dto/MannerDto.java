package com.example.safe_ride.matching.dto;

import com.example.safe_ride.matching.entity.Manner;
import com.example.safe_ride.member.dto.MemberDto;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MannerDto {
    private Long id;
    private MemberDto ratedMember; // 평가 받는 회원의 정보
    private MemberDto raterMember; // 평가하는 회원의 정보
    private int score; // 매너 점수
    private String comment; // 추가적인 코멘트
    private MatchingDto matchingId; // 매칭 ID

    // 엔티티를 DTO로 변환
    public static MannerDto fromEntity(Manner manner) {
        return MannerDto.builder()
                .id(manner.getId())
                .ratedMember(MemberDto.fromEntity(manner.getRatedMember()))
                .raterMember(MemberDto.fromEntity(manner.getRaterMember()))
                .score(manner.getScore())
                .comment(manner.getComment())
                .matchingId(MatchingDto.fromEntity(manner.getMatching()))
                .build();
    }
}
