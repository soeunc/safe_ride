package com.example.safe_ride.member;

import com.example.safe_ride.member.entity.Authority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/safe-ride")
public class MemberController {
    private final UserDetailsManager manager;
    private final PasswordEncoder passwordEncoder;
    //메인화면
    @GetMapping
    public String home(){
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
        if (!manager.userExists(dto.username)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        UserDetails userDetails = manager.loadUserByUsername(dto.username);
        log.info("username: {}", userDetails.getUsername());
        log.info("password: {}", userDetails.getPassword());

        if (!passwordEncoder.matches(dto.password, userDetails.getPassword())) {
            log.error("비밀번호가 일치하지 않습니다.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return "redirect:/safe-ride";
    }
    //로그아웃
    @PostMapping("/logout")
    public String logout() {
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
            @RequestParam("userId")
            String userId,
            @RequestParam("password")
            String password,
            @RequestParam("passwordCk")
            String passwordCk,
            @RequestParam("email")
            String email,
            @RequestParam("nickname")
            String nickname,
            @RequestParam("phoneNumber")
            String phoneNumber,
            @RequestParam("birthday")
            String birthday
    ){
        if (password.equals(passwordCk)){
            manager.createUser(CustomMemberDetails.builder()
                    .userId(userId)
                    .password(passwordEncoder.encode(password))
                    .email(email)
                    .nickname(nickname)
                    .phoneNumber(phoneNumber)
                    .birthday(birthday)
                    .authority(Authority.ROLE_INACTIVE_USER)
                    .build()
            );
        }
        return "redirect:/login";
    }
    //마이페이지
    @GetMapping("/myprofile")
    public String myprofile(){
        return "member/myprofile";
    }
}
