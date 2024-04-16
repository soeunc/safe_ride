package com.example.safe_ride.myPage.controller;

import com.example.safe_ride.myPage.service.MyPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
//라이딩기록과 매칭기록을 마이페이지에 보여준다.
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/safe-ride")
public class MyPageController {
    private final UserDetailsManager manager;
    private final MyPageService myPageService;


}
