package com.example.safe_ride.weather.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    // 현재날짜형식을 yyyymmdd 형태로 리턴
    static public String getCurrentDate(){
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return currentDate.format(dateFormatter);

    }

    // 현재시간보다 time 시간 전 시간을 리턴
    static public String getBeforeTime(int time){
        LocalTime currentTime = LocalTime.now();
        LocalTime oneHourAgo = currentTime.minusHours(time);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH");
        return oneHourAgo.format(formatter);
    }
}
