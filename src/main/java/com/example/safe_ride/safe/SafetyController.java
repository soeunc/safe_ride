package com.example.safe_ride.safe;

import com.example.safe_ride.safe.service.SafetyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SafetyController {
    private final SafetyService service;

//    @GetMapping("/safety-direction")
//    public String mapView() {
//        return "safety/main";
//    }

    @GetMapping("/safety-direction/location")
    public String location(Model model) {
        List<String> location = service.fetchDataFromApi("41", "150");   // 경기도, 의정부시 기준 검색
        model.addAttribute("location", location);
        return "safety/location";
    }

    // 시도, 군면 정보는 필수
    // 법정동 코드를 비교해서 일치 시 주변 사고정보를 불러오도록 하기
    @GetMapping("/safety-direction")
    public String showAccidents(
//            @RequestParam String siDo,
//            @RequestParam String guGun,
            Model model
    ) {
        // 사고다발 지역 데이터 가져오기
        List<String> accidentData = service.fetchDataFromApi("41", "150");

        // 모델에 데이터 추가
        model.addAttribute("accidents", accidentData);

        // 뷰 이름 반환
        return "safety/main";
    }

}