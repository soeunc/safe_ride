package com.example.safe_ride.myPage.repo;

import com.example.safe_ride.myPage.entity.QMyPage;
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
                        qMyPage.memberId.eq(memberId),
                        qMyPage.createDate.between(start, end)
                    )
                    .fetchOne();
        //주간 라이딩 기록이 없을 시 0으로 보여준다.
        if (weeklyRecord == null)
            weeklyRecord = 0;
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
                        qMyPage.memberId.eq(memberId)
                )
                .fetchOne();

        return getTotalRecord;
    }

    //날짜별 주간 라이딩 기록
    //일 -> 토 순서로 가져옴
    public List<Integer> getWeeklyRecordList(Long memberId, LocalDateTime start, LocalDateTime end){
        List<Integer> weeklyRecordList
                = queryFactory
                .select(
                        qMyPage.todayRecord
                )
                .from(qMyPage)
                .where(
                        qMyPage.memberId.eq(memberId),
                        qMyPage.createDate.between(start, end)
                )
                .orderBy(qMyPage.createDate.asc())
                .fetch();
        return weeklyRecordList;
    }
}
