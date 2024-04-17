package com.example.safe_ride.locationInfo.controller;

import com.example.safe_ride.locationInfo.service.AddressService;
import com.example.safe_ride.locationInfo.service.LocationInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public-bicycle")
@RequiredArgsConstructor
public class AddressController {

    private final LocationInfoService locationInfoService;
    private final AddressService addressService;

    @GetMapping("/sigungu")
    public ResponseEntity<List<String>> getSigunguBySido(
            @RequestParam("sido")
            String sido
    ) {
        List<String> sigunguList = addressService.getSigunguBySido(sido);
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
        List<String> eupmyundongList = addressService.getEupmyundongBySigungu(sigungu);
        if (eupmyundongList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        //상태 코드(http 200)와 함께 읍면동 목록을 JSON 형식으로 클라이언트에 반환
        return ResponseEntity.ok(eupmyundongList);
    }
}