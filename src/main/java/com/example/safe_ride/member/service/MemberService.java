package com.example.safe_ride.member.service;

import com.example.safe_ride.member.dto.JoinDto;
import com.example.safe_ride.member.entity.*;
import com.example.safe_ride.member.repo.BadgeRepo;
import com.example.safe_ride.member.repo.MemberRepo;
import com.example.safe_ride.member.dto.MemberDto;
import com.example.safe_ride.member.dto.UpdateDto;
import com.example.safe_ride.myPage.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepo memberRepo;
    private final UserDetailsManager manager;
    private final PasswordEncoder passwordEncoder;
    private final MyPageService myPageService;
    private final BadgeRepo badgeRepo;

    public MemberDto readMember(String userId){
        Member member = memberRepo.findByUserId(userId)
                .orElseThrow(
                        ()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")
                );
        return MemberDto.fromEntity(member);
    }
    @Transactional
    public void updateMember(String userId, UpdateDto dto) {

        Member member = memberRepo.findByUserId(userId)
                .orElseThrow(
                        ()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")
                );
        //비밀번호와 비밀번호 체크가 동일한가
        if (!dto.getPassword().equals(dto.getPasswordCk())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호와 비밀번호 체크가 일치하지 않습니다.");
        }
        
        //null과 공백 체크 and 값이 변경되었다면
        if (!ObjectUtils.isEmpty(dto.getPassword()) &&
            !dto.getPassword().equals(member.getPassword())){
            member.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (!ObjectUtils.isEmpty(dto.getEmail()) &&
            !dto.getEmail().equals(member.getEmail())){
            member.setEmail(dto.getEmail());
        }
        if (!ObjectUtils.isEmpty(dto.getNickName()) &&
            !dto.getNickName().equals(member.getNickname())){
            member.setNickname(dto.getNickName());
        }
        if (!ObjectUtils.isEmpty(dto.getPhoneNumber()) &&
            !dto.getPhoneNumber().equals(member.getPhoneNumber())){
            member.setPhoneNumber(dto.getPhoneNumber());
        }
        if (!ObjectUtils.isEmpty(dto.getBirthday()) &&
            !dto.getBirthday().equals(member.getBirthday())){
            member.setBirthday(dto.getBirthday());
        }

        memberRepo.save(member);

    }

    //id찾기
    public Long readMemberId(String userId){
        Member member = memberRepo.findByUserId(userId)
                .orElseThrow(
                        ()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")
                );
        return member.getId();
    }

    //회원가입
    @Transactional
    public void join(JoinDto dto){
         manager.createUser(CustomMemberDetails.builder()
                .userId(dto.getUserId())
                .userName(dto.getUserName())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .nickname(dto.getNickName())
                .phoneNumber(dto.getPhoneNumber())
                .birthday(dto.getBirthday())
                .authority(Authority.ROLE_INACTIVE_USER)
                .build()
        );
    }

    //뱃지 생성
    //등급 뱃지 하나 추가(킥보드 등급)
    @Transactional
    public Badge createBadge(String userId){
        Member member = memberRepo.findByUserId(userId)
                .orElseThrow(
                        ()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")
                );
        Badge badge = Badge.builder()
                .member(member)
                .grade(Grade.QUICK_BOARD)
                .build();
        return badgeRepo.save(badge);
    }
}
