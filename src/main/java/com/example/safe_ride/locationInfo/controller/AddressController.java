package com.example.safe_ride.locationInfo.controller;

import com.example.safe_ride.safe.dto.PointDto;
import com.example.safe_ride.locationInfo.service.LocationInfoService;
import com.example.safe_ride.safe.dto.rgeocoding.RGeoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/public-bicycle")
@RequiredArgsConstructor
public class AddressController {

    private final LocationInfoService locationInfoService;


    @GetMapping("/sigungu")
    public ResponseEntity<List<String>> getSigunguBySido(
            @RequestParam("sido")
            String sido
    ) {
        List<String> sigunguList = locationInfoService.getSigunguBySidoList(sido);
        if (sigunguList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        //상태 코드(http 200)와 함께 시군구 목록을 JSON 형식으로 클라이언트에 반환
        return ResponseEntity.ok(sigunguList);
    }

    @GetMapping("/eupmyundong")
    public ResponseEntity<List<String>> getEupmyundongBySigungu(
            @RequestParam("sigungu")
            String sigungu
    ) {
        List<String> eupmyundongList = locationInfoService.getEupmyundongBySigunguList(sigungu);
        if (eupmyundongList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        //상태 코드(http 200)와 함께 읍면동 목록을 JSON 형식으로 클라이언트에 반환
        return ResponseEntity.ok(eupmyundongList);
    }

}