package com.example.safe_ride.myPage.controller;

import com.example.safe_ride.facade.AuthenticationFacade;
import com.example.safe_ride.member.dto.UpdateDto;
import com.example.safe_ride.member.service.MemberService;
import com.example.safe_ride.myPage.service.MyPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
//라이딩기록과 매칭기록을 마이페이지에 보여준다.
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/safe-ride/myprofile")
public class MyPageController {
    private final UserDetailsManager manager;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationFacade authFacade;
    private final MemberService memberService;
    private final MyPageService myPageService;
    String msg = "";
    //마이페이지
    @GetMapping
    public String myprofile(
            Model model
    ){
        //사용자 개인정보 가져오기
        model.addAttribute(
                "member",
                memberService.readMember(authFacade.getAuth().getName())
        );
        return "member/myprofile";
    }
    //마이페이지 수정
    @PostMapping("/update")
    public String updateProfile(
//            @RequestBody
            UpdateDto dto,
            Model model
    ) {
        memberService.updateMember(authFacade.getAuth().getName(), dto);

        msg = "수정되었습니다.^^";

        model.addAttribute("msg", msg);
        return "redirect:/safe-ride/myprofile";
    }

    //@GetMapping("")
}
