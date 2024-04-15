package com.example.safe_ride.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherDetailVo {

    String unitName;
    String unitCode;
    String routeName;
    String routeNo;
    String stdHour;
    String updownTypeCode;
    String xValue;
    String yValue;
    String sdate;
    String snowValue;
    String addr;
    String weatherContents;
    String tempValue;
    String dewValue;
}
