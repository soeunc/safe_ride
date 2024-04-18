package com.example.safe_ride.safe.controller;

import com.example.safe_ride.safe.entity.AccidentInfo;
import com.example.safe_ride.safe.service.SafetyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SafetyController {
    private final SafetyService service;

    // 사고다발 지역 정보 추출 test
    @GetMapping("/safety-direction/location")
    public String location(Model model) {
        List<AccidentInfo> location = service.fetchDataFromApi();
        model.addAttribute("location", location);
        return "safety/location";
    }

    // 나중에 메인페이지 불러오는 것으로 변경
    // 법정동 코드를 비교해서 일치 시 주변 사고정보를 불러오도록 하기
    @GetMapping("/safety-direction")
    public String showAccidents(Model model) {
        // 사고다발 지역 데이터 가져오기
        List<AccidentInfo> accidentData = service.fetchDataFromApi();
        service.saveAccidentInfo(accidentData);
        // 모델에 데이터 추가
        // test로 리스트 1번째의 법정동 코드 보여주기
        model.addAttribute("accidents", accidentData.get(0).getBjDongCode());
        model.addAttribute("lnt", accidentData.get(0).getLoCrd());
        model.addAttribute("lat", accidentData.get(0).getLaCrd());


        // 뷰 이름 반환
        return "safety/main";
    }
}