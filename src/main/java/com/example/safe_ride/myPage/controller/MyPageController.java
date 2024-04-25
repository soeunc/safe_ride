package com.example.safe_ride.myPage.controller;

import com.example.safe_ride.facade.AuthenticationFacade;
import com.example.safe_ride.member.dto.JoinDto;
import com.example.safe_ride.member.dto.MemberDto;
import com.example.safe_ride.member.dto.UpdateDto;
import com.example.safe_ride.member.entity.Member;
import com.example.safe_ride.member.service.MemberService;
import com.example.safe_ride.myPage.dto.TodayRecordDto;
import com.example.safe_ride.myPage.service.MyPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        MemberDto memberDto = memberService.readMember(authFacade.getAuth().getName());
        model.addAttribute("member", memberDto);
        //라이딩 정보 가져오기
        model.addAttribute(
                "ridingRecord",
                myPageService.readRidingRecord(memberDto.getId()));
        //뱃지 가져오기
        model.addAttribute("badges", myPageService.readBadges(memberDto.getId()));
        return "member/myprofile";
    }
    //마이페이지 수정
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<UpdateDto> updateProfile(
            @RequestBody
            UpdateDto dto,
            RedirectAttributes redirectAttributes
    ) {
        memberService.updateMember(authFacade.getAuth().getName(), dto);
        msg = "수정되었습니다.^^";
        redirectAttributes.addFlashAttribute("msg", msg);
        return ResponseEntity.ok().body(dto);
    }
    //오늘 주행기록 입력
    @PostMapping("/create-today")
    public ResponseEntity<TodayRecordDto> createToday(
            @RequestBody
            TodayRecordDto dto,
            RedirectAttributes redirectAttributes
    ){
        Long memberId = memberService.readMemberId(authFacade.getAuth().getName());
        myPageService.createToday(memberId, dto.getTodayRecord());
        msg = "오늘 기록이 추가되었습니다.^^";
        redirectAttributes.addFlashAttribute("msg", msg);
        return ResponseEntity.ok().body(dto);
    }


}
