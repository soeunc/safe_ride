package com.example.safe_ride.myPage.repo;

import com.example.safe_ride.myPage.dto.WeeklyRecordDto;
import com.example.safe_ride.myPage.entity.QMyPage;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MyPageRepoDsl {
    @Autowired
    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;

    private QMyPage qMyPage = QMyPage.myPage;
    //주간 라이딩 기록
    public int getTotalWeeklyRecord(Long memberId, LocalDateTime start, LocalDateTime end){
         Integer weeklyRecord = queryFactory
                    .select(
                        qMyPage.todayRecord.sum()
                    )
                    .from(qMyPage)
                    .where(
                        qMyPage.member.id.eq(memberId),
                        qMyPage.createDate.between(start, end)
                    )
                    .fetchOne();
        return weeklyRecord;
    }
    //전체 라이딩 기록
    public int getTotalRecord(Long memberId){
        Integer getTotalRecord = queryFactory
                .select(
                        qMyPage.todayRecord.sum()
                )
                .from(qMyPage)
                .where(
                        qMyPage.member.id.eq(memberId)
                )
                .fetchOne();

        return getTotalRecord;
    }

    //날짜별 주간 라이딩 기록
    //일 -> 토 순서로 가져옴
    public List<WeeklyRecordDto> getWeeklyRecordList(Long memberId, LocalDateTime start, LocalDateTime end){
        List<WeeklyRecordDto> weeklyRecordList
                = queryFactory
                .select(
                        Projections.constructor(WeeklyRecordDto.class,
                            qMyPage.todayRecord,
                            qMyPage.createDate
                        )
                )
                .from(qMyPage)
                .where(
                        qMyPage.member.id.eq(memberId),
                        qMyPage.createDate.between(start, end)
                )
                .orderBy(qMyPage.createDate.asc())
                .fetch();
        return weeklyRecordList;
    }
}
