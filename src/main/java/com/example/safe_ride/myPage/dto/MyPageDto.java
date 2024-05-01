package com.example.safe_ride.myPage.dto;

import com.example.safe_ride.myPage.entity.MyPage;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MyPageDto {
    private Long id;
    private Long memberId;
    private Integer todayRecord;  // 오늘의 기록
    private LocalDateTime createDate;
    //private Manner manner;// 매칭 기록 id
    //전체기록
    private Integer totalRecord;
    //주간기록합산결과
    private Integer weeklyRecord;
    //주간 개별 기록
    private List<WeeklyRecordDto> weeklyRecordList;


    public static MyPageDto fromEntity(MyPage entity){
//        Manner manner = Optional.ofNullable(entity.getManner())
//                .orElse(new Manner());
        return MyPageDto.builder()
                .id(entity.getId())
                .memberId(entity.getMember().getId())
                .todayRecord(entity.getTodayRecord())
                .createDate(LocalDateTime.now())
                .build();
    }

}
