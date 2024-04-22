package com.example.safe_ride.matching.entity;

import com.example.safe_ride.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Getter
@Entity
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class MatchingApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id")
    private Matching matching;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id")
    private Member applicant;

    private String message;

    @Setter
    @Enumerated(EnumType.STRING)
    private MatchingStatus status;          // 승인 상태

    public void accept() {
        this.status = MatchingStatus.ACCEPTED;
    }

    public void reject() {
        this.status = MatchingStatus.REJECTED;
    }
}