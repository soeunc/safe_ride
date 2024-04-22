package com.example.safe_ride.locationInfo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class StationAndBicycleInfo {
    private StationInfo stationInfo;
    private BicycleInfo bicycleInfo;
}
