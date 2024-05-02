package com.example.safe_ride.safe.dto.geocoding;

import lombok.Data;

@Data
public class GeoMeta {
    private Integer totalCount; // 총 검색 건수
    private Integer page;
    private Integer count;
}
