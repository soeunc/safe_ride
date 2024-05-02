package com.example.safe_ride.member.dto;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDto {
    private String password;
    private String passwordCk;
    private String nickName;
    private String phoneNumber;
}
