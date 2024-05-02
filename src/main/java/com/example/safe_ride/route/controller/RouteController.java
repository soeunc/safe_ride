package com.example.safe_ride.route.controller;


import com.example.safe_ride.facade.AuthenticationFacade;
import com.example.safe_ride.member.service.MemberService;
import com.example.safe_ride.myPage.service.MyPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("route")
@Controller
@RequiredArgsConstructor
public class RouteController {
    private final AuthenticationFacade authFacade;
    private final MemberService memberService;
    private final MyPageService myPageService;
    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @GetMapping
    public String getMap(Model model) {
        model.addAttribute("kakaoApiKey", kakaoApiKey);
        return "route";
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity stopRiding(@RequestParam("totalRiding") String totalRiding) {
        Long memberId = memberService.readMemberId(authFacade.getAuth().getName());
        myPageService.createToday(memberId, Math.round(Integer.parseInt(totalRiding)));
        return new ResponseEntity<>("success", HttpStatus.OK);
    }
}
