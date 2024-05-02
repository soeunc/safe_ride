package com.example.safe_ride.locationInfo.service;

import com.example.safe_ride.locationInfo.entity.TempCombinedInfo;
import com.example.safe_ride.locationInfo.repo.TempCombinedInfoRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TempTableService {
    private final TempCombinedInfoRepo tempRepo;

    // 테이블 삭제와 테이블 저장을 하나의 생명주기로 관리
    @Transactional
    public void insertData(List<TempCombinedInfo> tempInfoList) {
        clearData();
        tempRepo.saveAll(tempInfoList);
    }

    public void clearData(){
        tempRepo.deleteAll();
    }

    public List<TempCombinedInfo> fetchData() {
        return tempRepo.findAll();
    }
}
