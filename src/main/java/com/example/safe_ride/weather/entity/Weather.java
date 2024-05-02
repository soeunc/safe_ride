package com.example.safe_ride.weather.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter
@Entity
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class Weather {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ridingStatus;   // 라이딩 상태
    private Double temperature;    // 온도
    private Integer precipitation; // 강수량
    private String windDirection;  // 풍향
    private Integer windSpeed;     // 풍량
    private String fineDust;       // 미세먼지 상태
    private String ultraFineDust;  // 초미세먼지 상태
}
