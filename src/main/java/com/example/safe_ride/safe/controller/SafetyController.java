package com.example.safe_ride.safe.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class SafetyController {

    // 메인 페이지 이동
    @GetMapping("/safety-direction")
    public String showAccidents() {
        return "/safetyDirectionInfo";
    }
}