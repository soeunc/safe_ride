package com.example.safe_ride.safe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CoordinateDto {
    private String lnt;  // 경도
    private String lat;  // 위도
}
