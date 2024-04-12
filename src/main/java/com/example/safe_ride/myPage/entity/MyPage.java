package com.example.safe_ride.myPage.entity;

import com.example.safe_ride.matching.entity.Manner;
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
public class MyPage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long memberId;
    private Integer record;       // 라이딩 기록
    private Integer weeklyRecord; // 주간 기록
    private Integer todayRecord;  // 오늘의 기록

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manner_id")
    private Manner manner;   // 매칭 기록
}
