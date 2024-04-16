package com.example.safe_ride.myPage.service;

import com.example.safe_ride.matching.entity.Manner;
import com.example.safe_ride.matching.entity.Matching;
import com.example.safe_ride.myPage.dto.MyPageDto;
import com.example.safe_ride.myPage.entity.MyPage;
import com.example.safe_ride.myPage.repo.MyPageRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private final MyPageRepo myPageRepo;
    //라이딩 정보 가져오기
    @Transactional
    public MyPageDto readRidingRecord(Long memberId){
        Optional<MyPage> optionalMyPage = myPageRepo.findByMemberId(memberId);
        MyPageDto myPageDto = new MyPageDto();
        MyPage myPage = new MyPage();
        //마이페이지 값이 존재한다면
        if (optionalMyPage.isPresent()){
            myPage = optionalMyPage.get();
        } 
        //존재하지 않는다면
        else {
            //새로 생성
            createRecord(memberId);
            //다시불러오기
            optionalMyPage = myPageRepo.findByMemberId(memberId);
            myPage = optionalMyPage.get();
        }
        myPageDto = MyPageDto.fromEntity(myPage);
        return myPageDto;
    }
    //오늘 주행기록 입력
    @Transactional
    public void createToday(Long memberId, int todayRecord){
        MyPage myPage = myPageRepo.findByMemberId(memberId).orElseThrow(
                ()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "라이딩 정보를 찾을 수 없습니다.")
        );
        //총 / 주간 / 오늘 주행기록에 더해주기
        myPage.setRecord(myPage.getRecord() + todayRecord);
        myPage.setWeeklyRecord(myPage.getWeeklyRecord() + todayRecord);
        myPage.setTodayRecord(myPage.getTodayRecord() + todayRecord);
    }
    //빈 주행기록 생성
    //마이페이지 방문 시 주행기록이 없는 신규유저 대상 목적
    @Transactional
    public void createRecord(Long memberId){
        MyPage myPage = MyPage.builder()
                .memberId(memberId)
                .record(0)
                .weeklyRecord(0)
                .todayRecord(0)
                .manner(null)
                .build();

        myPageRepo.save(myPage);
    }

}
