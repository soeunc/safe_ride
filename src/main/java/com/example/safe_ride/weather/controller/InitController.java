package com.example.safe_ride.weather.controller;

import com.example.safe_ride.weather.dto.ResponseVo;
import com.example.safe_ride.weather.dto.WeatherRequestVo;
import com.example.safe_ride.weather.dto.WeatherResponseVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequiredArgsConstructor
@Slf4j
public class InitController {

    @GetMapping("/hello")
    public ResponseEntity<ResponseVo> initApi(@RequestParam String input){
        ResponseVo responseVo = new ResponseVo();
        responseVo.setUcd("00");
        responseVo.setMessage("수신 된 값 : " + input);
        return ResponseEntity.ok(responseVo);
    }

    @GetMapping("/testOpenAPI")
    public ResponseEntity<ResponseVo> testOpenAPI(@RequestParam String date){

        //https://data.ex.co.kr/openapi/restinfo/restWeatherList?key=test&type=xml&sdate=20240415&stdHour=10
        //위 api를 get http 호출을 해서 전국휴게소날씨를 출력해보자

        // requestVo 정의
        WeatherRequestVo weatherRequestVo = new WeatherRequestVo();
        weatherRequestVo.setKey("test");
        weatherRequestVo.setType("json");
        weatherRequestVo.setSdate(date);
        //현재 시간 HH 기준으로 가져오기

        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH");
        weatherRequestVo.setStdHour(currentTime.format(formatter));


        WebClient webClient = WebClient.builder()
                .baseUrl("https://data.ex.co.kr/openapi/restinfo")
                .build();

        String apiUrl = "/restWeatherList?key={apikey}&type={type}&sdate={sdate}&stdHour={stdHour}";
        WeatherResponseVo weatherResponse = webClient.get()
                .uri(apiUrl, weatherRequestVo.getKey(),
                        weatherRequestVo.getType(),
                        weatherRequestVo.getSdate(),
                        weatherRequestVo.getStdHour())
                .retrieve()
                .bodyToMono(WeatherResponseVo.class)
                .block();

        log.info("response : " + weatherResponse.toString());

        ResponseVo responseVo = new ResponseVo();
        log.info("response VO count : " + weatherResponse.getCount());

        if (weatherResponse.getList().isEmpty()){
            responseVo.setUcd("99");
            responseVo.setMessage("날씨조회 실패");
        } else {
            responseVo.setUcd("00");
            responseVo.setMessage(weatherResponse.getList().toString());
        }
        return ResponseEntity.ok(responseVo);
    }
}
