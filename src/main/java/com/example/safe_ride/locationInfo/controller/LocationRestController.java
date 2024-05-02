package com.example.safe_ride.locationInfo.controller;

import com.example.safe_ride.locationInfo.dto.LocationInfoResponseDto;
import com.example.safe_ride.locationInfo.service.LocationInfoService;
import com.example.safe_ride.locationInfo.service.PublicApiService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/public-bicycle")
@RequiredArgsConstructor
public class LocationRestController {
    private final LocationInfoService locationInfoService;
    private final PublicApiService apiService;


    // 주소 검색 : 주소API 검색결과 -> 서버 저장
    @PostMapping("/getAddressPosition")
    public ResponseEntity<Void> receiveAddressPosition(
            @RequestParam
            String roadAddrPart1,
            HttpSession session
    ) throws IOException {
        log.info("On getAddressPostion");
        LocationInfoResponseDto infoResponse = apiService.getLocationInfo(roadAddrPart1);

        locationInfoService.processData(infoResponse.getSearchedPoint());

        session.setAttribute("infoResponse", infoResponse);
        session.setAttribute("isSearched", true);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/public-bicycle"))
                .build();
    }

    // 위치 검색 : 브라우저 좌표 -> 서버 전달
    @PostMapping("/getUserPosition")
    public ResponseEntity<Void> receiveUserPosition(
            @RequestBody
            Map<String, Double> payload,
            HttpSession session
    ) throws IOException {
        LocationInfoResponseDto infoResponse = apiService.getLocationInfo(payload);

        locationInfoService.processData(infoResponse.getSearchedPoint());

        session.setAttribute("infoResponse", infoResponse);
        session.setAttribute("isSearched", true);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/public-bicycle"))
                .build();
    }

}
