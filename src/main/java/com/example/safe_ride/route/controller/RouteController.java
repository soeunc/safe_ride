package com.example.safe_ride.route.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("route")
@Controller
public class RouteController {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @GetMapping
    public String getMap(Model model) {
        model.addAttribute("kakaoApiKey", kakaoApiKey);
        return "route";
    }
}
