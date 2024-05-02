package com.example.safe_ride.safe.dto.geocoding;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GeoNcpResponse {
    private String status;  // 검색 결과 상태 코드
    private GeoMeta meta;   // 검색 메타 데이터
    private List<GeoAddress> addresses; // 주소 검색 결과 목록
    private String errorMessage;  // 예외 발생 시 메시지
}
