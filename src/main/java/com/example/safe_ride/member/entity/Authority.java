package com.example.safe_ride.member.entity;

import lombok.Getter;

@Getter
public enum Authority {
    ROLE_INACTIVE_USER("비활성사용자"),
    ROLE_USER("일반사용자");
    private String authority;
    Authority(String authority) {
        this.authority = authority;
    }
}
