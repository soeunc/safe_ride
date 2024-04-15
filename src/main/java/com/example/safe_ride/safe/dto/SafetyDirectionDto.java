package com.example.safe_ride.safe.dto;

import lombok.Data;

@Data
public class SafetyDirectionDto {
    private String safetyInfor;  // 안전지역 안내
    private String address;      // 사용자 현재 위치(지번, 도로명)
}
