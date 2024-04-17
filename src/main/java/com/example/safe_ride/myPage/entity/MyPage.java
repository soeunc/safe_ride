package com.example.safe_ride.myPage.entity;

import com.example.safe_ride.matching.entity.Manner;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
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
    private Integer todayRecord;  // 오늘의 기록
    private LocalDateTime createDate;
    //MyPage(일일 라이딩 기록)과 라이더매칭-매너점수(Manner)간의
    //연관관계 재확인 후 건드리기 위해 잠시 주석
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "manner_id")
//    private Manner manner;   // 매칭 기록
}
