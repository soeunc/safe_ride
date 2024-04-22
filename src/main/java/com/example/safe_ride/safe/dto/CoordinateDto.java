package com.example.safe_ride.safe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CoordinateDto {
    private String accidentLng;  // 경도
    private String accidentLat;  // 위도
}
