package com.example.safe_ride.matching.dto;

import com.example.safe_ride.matching.entity.MatchingApplication;
import com.example.safe_ride.member.dto.MemberDto;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchingApplicationDto {
    private Long id;
    private Long matchingId;        // 매칭글 ID
    private MemberDto applicant;    // 신청자 정보
    private String message;         // 매칭 메시지 추가

    public static MatchingApplicationDto fromEntity(MatchingApplication application) {
        return MatchingApplicationDto.builder()
                .id(application.getId())
                .matchingId(application.getMatching().getId())
                .message(application.getMessage())
                .applicant(MemberDto.fromEntity(application.getApplicant()))
                .build();
    }
}
