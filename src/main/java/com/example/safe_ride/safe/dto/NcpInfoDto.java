package com.example.safe_ride.safe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NcpInfoDto {
    private Double lat;
    private Double lng;
    private String bjDongCode;
    private List<CoordinateDto> accidentCoordinates;
}
