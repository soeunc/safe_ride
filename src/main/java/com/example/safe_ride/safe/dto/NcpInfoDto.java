package com.example.safe_ride.safe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NcpInfoDto {
    private Double lat;
    private Double lng;
    private String bjDongCode;
}
