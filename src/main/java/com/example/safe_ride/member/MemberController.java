package com.example.safe_ride.member;

import com.example.safe_ride.facade.AuthenticationFacade;
import com.example.safe_ride.member.entity.Authority;
import com.example.safe_ride.member.entity.Member;
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

import java.io.IOException;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/safe-ride")
public class MemberController {
    private final UserDetailsManager manager;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationFacade authFacade;
    private final MemberService memberService;
    String msg = "";
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
    @PostMapping("/myprofile/update")
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
}
