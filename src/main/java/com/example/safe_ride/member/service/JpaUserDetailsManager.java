package com.example.safe_ride.member.service;


import com.example.safe_ride.member.entity.CustomMemberDetails;
import com.example.safe_ride.member.entity.Authority;
import com.example.safe_ride.member.entity.Member;
import com.example.safe_ride.member.repo.MemberRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JpaUserDetailsManager implements UserDetailsManager {
    private final MemberRepo memberRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Member> optionalMember = memberRepo.findByUserId(username);
        if (optionalMember.isEmpty())
            throw new UsernameNotFoundException(username);

        log.info("user id : {}",optionalMember.get().getUserId());
        Member member = optionalMember.get();

        CustomMemberDetails customMemberDetails = CustomMemberDetails.builder()
                .userId(member.getUserId())
                .userName(member.getUserName())
                .password(member.getPassword())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .phoneNumber(member.getPhoneNumber())
                .birthday(member.getBirthday())
                .authority(member.getAuthority())
                .build();

        return customMemberDetails;
    }

    @Override
    public void createUser(UserDetails user) {
        if (userExists(user.getUsername())) {
            log.error("이미 존재하는 아이디입니다.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (user instanceof CustomMemberDetails memberDetails) {
            Member newMember = Member.builder()
                    .userId(memberDetails.getUserId())
                    .userName(memberDetails.getUsername())
                    .password(memberDetails.getPassword())
                    .email(memberDetails.getEmail())
                    .nickname(memberDetails.getNickname())
                    .phoneNumber(memberDetails.getPhoneNumber())
                    .birthday(memberDetails.getBirthday())
                    .authority(Authority.ROLE_INACTIVE_USER)
                    .build();
            log.info("authority: {}", memberDetails.getAuthorities());

            if (emailExists(newMember.getEmail())) {
                log.error("이미 존재하는 이메일입니다.");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            memberRepo.save(newMember);
        } else {
            throw new IllegalArgumentException("Unsupported UserDetails type");
        }
    }

    @Override
    public void updateUser(UserDetails user) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public void deleteUser(String username) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public boolean userExists(String username) {
        return memberRepo.existsByUserId(username);
    }

    public boolean emailExists(String email) {
        return memberRepo.existsByEmail(email);
    }
}
