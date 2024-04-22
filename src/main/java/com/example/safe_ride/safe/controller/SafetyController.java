package com.example.safe_ride.safe.controller;

import com.example.safe_ride.safe.entity.AccidentInfo;
import com.example.safe_ride.safe.service.ApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SafetyController {
    private final ApiService apiService;

    // 사고다발 지역 정보 추출 test
    @GetMapping("/safety-direction/location")
    public String location(Model model) {
        List<AccidentInfo> location = apiService.fetchDataFromApi();
        model.addAttribute("location", location);
        return "safety/location";
    }


    // 메인 페이지 이동
    @GetMapping("/safety-direction")
    public String showAccidents() {
        return "safety/main";
    }
}