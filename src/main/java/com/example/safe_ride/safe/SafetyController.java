package com.example.safe_ride.safe;

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

    @GetMapping("/safety-direction")
    public String mapView() {
        return "safety/main";
    }

    @GetMapping("/safety-direction/location")
    public String location(Model model) {
        List<String> location = service.fetchDataFromApi("41", "150");   // 경기도, 의정부시 기준 검색
        model.addAttribute("location", location);
        return "safety/location";
    }

}