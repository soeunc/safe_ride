package com.example.safe_ride.weather.controller;

import com.example.safe_ride.weather.dto.WeatherDetailVo;
import com.example.safe_ride.weather.dto.WeatherRequestVo;
import com.example.safe_ride.weather.dto.WeatherResponseVo;
import com.example.safe_ride.weather.service.WeatherService;
import com.example.safe_ride.weather.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/weather/findAll")
    public ResponseEntity<List<WeatherDetailVo>> testOpenAPI(){

        // requestVo 정의
        WeatherRequestVo weatherRequestVo = new WeatherRequestVo();
        weatherRequestVo.setKey("test");
        weatherRequestVo.setType("json");
        weatherRequestVo.setSdate(DateUtil.getCurrentDate());
        weatherRequestVo.setStdHour(DateUtil.getBeforeTime(6));

        // API 호출을 통해 전국 휴게소 날씨 LIST 가져오기
        WeatherResponseVo weatherResponse = weatherService.getWeatherInfoList(weatherRequestVo);
        log.info("weatherResponse VO count : " + weatherResponse.getCount());

        // 전국 휴게소 날씨 리턴
        return ResponseEntity.ok(weatherResponse.getList());
    }
}
