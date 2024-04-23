package com.example.safe_ride.safe.controller;

import com.example.safe_ride.safe.dto.*;
import com.example.safe_ride.safe.service.NcpService;
import com.example.safe_ride.safe.service.SafetyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("safety-direction")
@RequiredArgsConstructor
public class NcpController {
    private final NcpService service;
    private final SafetyService safetyService;

    // 사용자의 현재 위치를 입력받아 좌표 반환
    @PostMapping("/check-test")
    public PointDto locateAddressTest(
            @RequestBody NaviWithQueryDto dto
    ) {
        return service.locateAddress(dto);
    }

    @PostMapping("/check")
    public ResponseEntity<?> locateAddress(
            @RequestBody NaviWithQueryDto dto,
            Model model
    ) {
        // 1. 사용자 위치 데이터 받기
        PointDto pointDto = service.locateAddress(dto);
        log.info("좌표 확인: {}", pointDto);
        if (pointDto == null) {
            return ResponseEntity.badRequest().body("주소로부터 좌표를 얻는 데 실패했습니다.");
        }

        // 2. 데이터 정보 불러오기
        // 2-1. 필더링된 사고정보 가져오기
        safetyService.saveFilteredAccidentInfo(pointDto);
        safetyService.saveFilteredSchoolZoneAccInfo(pointDto);

        // 2-2. DB에 저장된 좌표 및 정보 가져오기
        List<CoordinateDto> coordinates = safetyService.getCoordinates();
        List<SchoolZoneInfoDto> schoolZoneInfoList = safetyService.getSchoolZoneInfo();
        model.addAttribute("school", schoolZoneInfoList);
        log.info("사고위치 정보 :{}", coordinates);
        log.info("스쿨존 사고 정보: {}", schoolZoneInfoList);

        // 사용자 위치 좌표 및 사고 좌표
        NcpInfoDto ncpInfoDto = new NcpInfoDto();
        ncpInfoDto.setLng(pointDto.getLng());
        ncpInfoDto.setLat(pointDto.getLat());
        ncpInfoDto.setAccidentCoordinates(coordinates);
        ncpInfoDto.setSchoolZoneInfo(schoolZoneInfoList);

        return ResponseEntity.ok(ncpInfoDto);
    }

}
