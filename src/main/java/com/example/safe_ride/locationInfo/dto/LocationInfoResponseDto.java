package com.example.safe_ride.locationInfo.dto;

import com.example.safe_ride.locationInfo.entity.TempLocationInfoResponse;
import com.example.safe_ride.safe.dto.PointDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationInfoResponseDto {
    private PointDto searchedPoint;
    private String transCode;
    private String fullAddress;

    public static TempLocationInfoResponse toEntity(LocationInfoResponseDto dto) {
        return TempLocationInfoResponse.builder()
                .lat(dto.getSearchedPoint().getLat())
                .lng(dto.getSearchedPoint().getLng())
                .transCode(dto.getTransCode())
                .fullAddress(dto.getFullAddress())
                .build();
    }
}
