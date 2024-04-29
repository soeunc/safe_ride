package com.example.safe_ride.locationInfo.entity;

import com.example.safe_ride.locationInfo.dto.CombinedInfoDto;
import com.example.safe_ride.locationInfo.dto.LocationInfoResponseDto;
import com.example.safe_ride.safe.dto.PointDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TempLocationInfoResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double lng;
    private Double lat;
    private String transCode;
    private String fullAddress;

}
