package com.example.safe_ride.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherResponseVo {

    int count;
    ArrayList<WeatherDetailVo> list;
}
