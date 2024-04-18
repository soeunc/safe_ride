package com.example.safe_ride.safe.controller;

import com.example.safe_ride.safe.dto.NcpInfoDto;
import com.example.safe_ride.safe.dto.NaviWithQueryDto;
import com.example.safe_ride.safe.dto.PointDto;
import com.example.safe_ride.safe.dto.rgeocoding.RGeoResponseDto;
import com.example.safe_ride.safe.service.NcpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("safety-direction")
@RequiredArgsConstructor
public class NcpController {
    private final NcpService service;

    // 사용자의 현재 위치를 입력받아 좌표 반환
    @PostMapping("/check-test")
    public PointDto locateAddressTest(
            @RequestBody NaviWithQueryDto dto
    ) {
        return service.locateAddress(dto);
    }


    // 좌표와 법정동 코드도 같이 전달
    // TODO 폼에서 주소를 입력하는 곳이 사용자가 편리하지 않은 방식이다. 다른 방법으로 한번 생각해볼 필요 있음.
    @PostMapping("/check")
    public ResponseEntity<?> locateAddress(
            @RequestBody NaviWithQueryDto dto,
            Model model
    ) {
        PointDto pointDto = service.locateAddress(dto);
        log.info("좌표 확인: {}", pointDto);
        if (pointDto == null) {
            return ResponseEntity.badRequest().body("주소로부터 좌표를 얻는 데 실패했습니다.");
        }

       RGeoResponseDto rGeoResponseDto = service.getBjDongCode(pointDto);
        log.info("법정동 코드 확인: {}", rGeoResponseDto);
        if (rGeoResponseDto == null) {
            return ResponseEntity.badRequest().body("법정동 코드를 조회하는 데 실패했습니다.");
        }


        /*// 1. 데이터 저장 전 법정동 코드 일치 확인
        safetyService.saveFilteredAccidentInfo(pointDto);
        // 2. 일치한 사고다발 지역 데이터 가져오기
        List<AccidentInfo> accidentData = safetyService.fetchDataFromApi();
        log.info("사고다발 지역 데이터: {}", accidentData.get(0));

        // 모델에 데이터 추가
        // test로 리스트 1번째의 법정동 코드 보여주기
        model.addAttribute("accidents", accidentData.get(0).getBjDongCode());
        model.addAttribute("lnt", accidentData.get(0).getLoCrd());
        model.addAttribute("lat", accidentData.get(0).getLaCrd());*/


        // 사용자 위치 좌표 및 법정동 코드
        NcpInfoDto ncpInfoDto = new NcpInfoDto();
        ncpInfoDto.setLat(pointDto.getLat());
        ncpInfoDto.setLng(pointDto.getLng());
        ncpInfoDto.setBjDongCode(rGeoResponseDto.getBjDongCode());

        return ResponseEntity.ok(ncpInfoDto);
    }

}
