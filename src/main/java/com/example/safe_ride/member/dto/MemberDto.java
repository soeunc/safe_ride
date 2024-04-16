package com.example.safe_ride.member.dto;

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
//        if (entity == null) {
//            return null; // 또는 적절한 처리를 수행할 수 있습니다.
//        }
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
