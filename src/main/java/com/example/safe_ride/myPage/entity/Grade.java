package com.example.safe_ride.myPage.entity;

import lombok.Getter;

@Getter
public enum Grade {
    //킥보드 -> 자전거 -> 스쿠터 -> 오토바이
    QUICK_BOARD("킥보드"),
    BYCICLE("자전거"),
    SCOOTER("스쿠터"),
    MOTOR_CYCLE("오토바이");

    private String grade;
    Grade(String grade) { this.grade = grade; }
}
