package com.example.safe_ride.myPage.service;

import com.example.safe_ride.myPage.dto.MyPageDto;
import com.example.safe_ride.myPage.entity.MyPage;
import com.example.safe_ride.myPage.repo.MyPageRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private final MyPageRepo myPageRepo;
    //라이딩 정보 가져오기
    public MyPageDto readRidingRecord(Long memberId){
        MyPage myPage = myPageRepo.findByMemberId(memberId).orElseThrow(
                ()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "라이딩 정보를 찾을 수 없습니다.")
        );
        return MyPageDto.fromEntity(myPage);
    }
}
