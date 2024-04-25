package com.example.safe_ride.matching.entity;

import com.example.safe_ride.article.entity.Region;
import com.example.safe_ride.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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
    private String title;

    @Setter
    private LocalDateTime ridingTime; // 라이딩 시간

    @Setter
    private String kilometer;

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

}

