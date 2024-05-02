package com.example.safe_ride.safe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PointDto {
    private Double lng; // 경도
    private Double lat; // 위도

    public String toQueryValue() {
        return String.format("%f,%f", lng, lat);
    }
}
