package com.example.safe_ride.myPage.dto;

import com.example.safe_ride.matching.entity.Manner;
import com.example.safe_ride.myPage.entity.MyPage;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MyPageDto {
    private Long id;
    private Long memberId;
    private Integer record;       // 라이딩 기록
    private Integer weeklyRecord; // 주간 기록
    private Integer todayRecord;  // 오늘의 기록
    private Long mannerId;// 매칭 기록 id

    public static MyPageDto fromEntity(MyPage entity){
        return MyPageDto.builder()
                .id(entity.getId())
                .mannerId(entity.getId())
                .record(entity.getRecord())
                .weeklyRecord(entity.getWeeklyRecord())
                .todayRecord(entity.getTodayRecord())
                .mannerId(entity.getManner().getId())
                .build();
    }
}
