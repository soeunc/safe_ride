package com.example.safe_ride.matching.entity;

import com.example.safe_ride.article.entity.Region;
import com.example.safe_ride.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Matching {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;          // 매칭글 작성자


    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;
    private String comment;         // 매칭 코멘트
    private Timestamp createTime;   // 작성 시간
    @Setter
    @Enumerated(EnumType.STRING)
    private MatchingStatus status;          // 승인 상태

    @OneToMany(mappedBy = "matching", cascade = CascadeType.ALL)
    private List<MatchingApplication> applications;

    public void accept() {
        this.status = MatchingStatus.ACCEPTED;
    }

    public void reject() {
        this.status = MatchingStatus.REJECTED;
    }
}

