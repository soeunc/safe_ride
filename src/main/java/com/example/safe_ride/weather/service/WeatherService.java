package com.example.safe_ride.weather.service;

import com.example.safe_ride.weather.dto.WeatherRequestVo;
import com.example.safe_ride.weather.dto.WeatherResponseVo;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class WeatherService {

    public WeatherResponseVo getWeatherInfoList(WeatherRequestVo weatherRequestVo){

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

        return weatherResponse;
    }
}
