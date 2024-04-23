package com.example.safe_ride.member.controller;

import com.example.safe_ride.facade.AuthenticationFacade;
import com.example.safe_ride.member.dto.JoinDto;
import com.example.safe_ride.member.dto.LoginDto;
import com.example.safe_ride.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/safe-ride")
public class MemberController {
    private final UserDetailsManager manager;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationFacade authFacade;
    private final MemberService memberService;
    private static String msg = "";
    //메인화면
    @GetMapping
    public String home(
            Model model
    ){
        model.addAttribute("userId", authFacade.getAuth().getName());
        return "home";
    }
    //로그인 화면
    @GetMapping("/login")
    public String loginForm(){
        return "member/login-form";
    }
    //로그인
    @PostMapping("/login")
    public String login(
        @RequestBody
        LoginDto dto
    ){
        if (!manager.userExists(dto.getUsername())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        UserDetails userDetails = manager.loadUserByUsername(dto.getUsername());
        log.info("username: {}", userDetails.getUsername());
        log.info("password: {}", userDetails.getPassword());

        if (!passwordEncoder.matches(dto.getPassword(), userDetails.getPassword())) {
            log.error("비밀번호가 일치하지 않습니다.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return "redirect:/safe-ride";
    }
    //로그아웃
    @PostMapping("/logout")
    public String logout(
            RedirectAttributes redirectAttributes
    ) {
        msg = "로그아웃 되었습니다 ^^.";
        redirectAttributes.addFlashAttribute("msg", msg);
        return "redirect:/safe-ride";
    }
    // 회원가입 화면
    @GetMapping("/join")
    public String signUpForm() {
        return "member/register-form";
    }
    //회원가입
    @PostMapping("/join")
    public String signUp(
            JoinDto dto,
            RedirectAttributes redirectAttributes
    ){
        if (dto.getPassword().equals(dto.getPasswordCk())){
            memberService.join(dto);
        }
        //뱃지 생성
        memberService.createBadge(dto.getUserId());
        msg = "회원가입 되었습니다 ^^.";
        redirectAttributes.addFlashAttribute("msg", msg);
        return "redirect:/safe-ride/login";
    }

    //아이디 중복확인
    @PostMapping("/duplicateCkForId")
    @ResponseBody
    public int duplicateCkForId(
            @RequestParam("userId")
            String userId
    ){
        return memberService.duplicateCkForId(userId);
    }

}
