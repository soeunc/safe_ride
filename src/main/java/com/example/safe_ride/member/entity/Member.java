package com.example.safe_ride.member.entity;

import com.example.safe_ride.myPage.entity.Badge;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Getter
@Setter
@Entity
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;
    private String userName;
    private String password;
    private String email;
    private String nickname;
    private String phoneNumber;
    private String birthday;
    private Authority authority;

}
