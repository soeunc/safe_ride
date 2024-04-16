package com.example.safe_ride.myPage.dto;

import com.example.safe_ride.matching.entity.Manner;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MyPageDto {
    private Long memberId;
    private Integer record;       // 라이딩 기록
    private Integer weeklyRecord; // 주간 기록
    private Integer todayRecord;  // 오늘의 기록
    private Long mannerId;// 매칭 기록 id
}
