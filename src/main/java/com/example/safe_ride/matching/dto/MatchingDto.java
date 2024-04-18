    package com.example.safe_ride.matching.dto;

    import com.example.safe_ride.matching.entity.Matching;
    import com.example.safe_ride.matching.entity.MatchingStatus;
    import com.example.safe_ride.member.entity.Member;
    import lombok.*;

    import java.sql.Timestamp;
    import java.util.Collections;
    import java.util.List;
    import java.util.stream.Collectors;
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class MatchingDto {
        private Long id;
        private Member member;          // 매칭글 작성자
        private String metropolitanCity; // 광역자치구
        private String city; // 도시
        private String comment;         // 매칭 코멘트
        private Timestamp createTime;   // 작성 시간
        private MatchingStatus status;  // 상태 Enum 타입으로 저장
        private List<MatchingApplicationDto> applications;  // 매칭 신청 목록
        private Long regionId; // Region 객체 대신 ID만 사용

        public static MatchingDto fromEntity(Matching matching) {
            MatchingDto matchingDto = MatchingDto.builder()
                    .id(matching.getId())
                    .metropolitanCity(matching.getRegion().getMetropolitanCity())
                    .member(matching.getMember())
                    .city(matching.getRegion().getCity())
                    .comment(matching.getComment())
                    .createTime(matching.getCreateTime())
                    .status(matching.getStatus()) // Enum 상태 직접 설정
                    .regionId(matching.getRegion().getId()) // Region 객체의 ID 사용
                    .build();

            // Matching 엔티티의 applications 속성이 null이 아닌 경우에만 처리
            if (matching.getApplications() != null) {
                matchingDto.setApplications(matching.getApplications().stream()
                        .map(MatchingApplicationDto::fromEntity)
                        .collect(Collectors.toList()));
            } else {
                matchingDto.setApplications(Collections.emptyList());
            }

            return matchingDto;
        }
    }
