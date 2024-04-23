package com.example.safe_ride.safe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SchoolZoneInfoDto {
    private String spotNm;
    private String schLng;
    private String schLat;
    private String occrrncCnt;
    private String dthDnvCnt;
    private String sidoSggNnm;

}
