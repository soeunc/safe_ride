package com.example.safe_ride.member;

import com.example.safe_ride.member.entity.Authority;
import com.example.safe_ride.member.entity.Member;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {
    private Long id;
    private String userId;
    private String userName;
    private String password;
    private String email;
    private String nickname;
    private String phoneNumber;
    private String birthday;
    private Authority authority;

    public static MemberDto fromEntity(Member entity){
        return MemberDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .userName(entity.getUserName())
                .password(entity.getPassword())
                .email(entity.getEmail())
                .nickname(entity.getNickname())
                .phoneNumber(entity.getPhoneNumber())
                .birthday(entity.getBirthday())
                .authority(entity.getAuthority())
                .build();
    }
}
