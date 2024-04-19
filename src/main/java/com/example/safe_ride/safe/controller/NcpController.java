package com.example.safe_ride.safe.controller;

import com.example.safe_ride.safe.dto.NcpInfoDto;
import com.example.safe_ride.safe.dto.NaviWithQueryDto;
import com.example.safe_ride.safe.dto.PointDto;
import com.example.safe_ride.safe.dto.rgeocoding.RGeoResponseDto;
import com.example.safe_ride.safe.entity.AccidentInfo;
import com.example.safe_ride.safe.service.ApiService;
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
    private final ApiService apiService;

    // 사용자의 현재 위치를 입력받아 좌표 반환
    @PostMapping("/check-test")
    public PointDto locateAddressTest(
            @RequestBody NaviWithQueryDto dto
    ) {
        return service.locateAddress(dto);
    }

    // 메인 페이지에서 사용자 위치 입력 시 사고다발지역 마커 표시
    // 입력을 받는 것과 데이터를 가져오는 것을 나눠야될지 고민...
    // TODO 폼에서 주소를 입력하는 곳이 사용자가 편리하지 않은 방식이다. 다른 방법으로 한번 생각해볼 필요 있음.
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

        // 2. 사용자 위치 법정동 코드 받기
       RGeoResponseDto rGeoResponseDto = service.getBjDongCode(pointDto);
        log.info("법정동 코드 확인: {}", rGeoResponseDto);
        if (rGeoResponseDto == null) {
            return ResponseEntity.badRequest().body("법정동 코드를 조회하는 데 실패했습니다.");
        }

        // 3. 데이터 정보 불러오기
        // 3-1. 사고다발 지역 데이터 가져오기
        List<AccidentInfo> accidentData = apiService.fetchDataFromApi();
        // 3-2. 필더링된 데이터 가져오기
        safetyService.saveFilteredAccidentInfo(pointDto);

        // 3-3. DB에 저장된 좌표 가져오기

//        model.addAttribute("accidents", accidentData.get(0).getBjDongCode());
//        model.addAttribute("lnt", accidentData.get(0).getLoCrd());
//        model.addAttribute("lat", accidentData.get(0).getLaCrd());


        // 사용자 위치 좌표 및 법정동 코드
        NcpInfoDto ncpInfoDto = new NcpInfoDto();
        ncpInfoDto.setLat(pointDto.getLat());
        ncpInfoDto.setLng(pointDto.getLng());
        ncpInfoDto.setBjDongCode(rGeoResponseDto.getBjDongCode());

        // 사용자의 좌표와 데이터의 좌표를 전달해야함
        // 지금은 사용자의 좌표만 전달 중
        return ResponseEntity.ok(ncpInfoDto);
    }

}
