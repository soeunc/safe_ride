package com.example.safe_ride.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherRequestVo {

    String key;
    String type;
    String sdate;
    String stdHour;

}

