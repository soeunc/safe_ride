package com.example.safe_ride.matching.entity;

import lombok.Getter;

@Getter
public enum MatchingStatus {
    PENDING("대기중"),
    ACCEPTED("수락"),
    REJECTED("거절"),
    END("종료");

    private final String status;

    MatchingStatus(String status) {
        this.status = status;
    }

}
