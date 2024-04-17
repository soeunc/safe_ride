package com.example.safe_ride.safe;

import com.example.safe_ride.safe.dto.LocationInfoDto;
import com.example.safe_ride.safe.dto.NaviWithQueryDto;
import com.example.safe_ride.safe.dto.PointDto;
import com.example.safe_ride.safe.dto.rgeocoding.RGeoResponseDto;
import com.example.safe_ride.safe.service.NcpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("safety-direction")
@RequiredArgsConstructor
public class NcpController {
    private final NcpService service;

    // 사용자의 현재 위치를 입력받아 좌표 반환
//    @PostMapping("/check")
//    public PointDto locateAddress(
//            @RequestBody NaviWithQueryDto dto
//            ) {
//        return service.locateAddress(dto);
//    }

    // 좌표를 입력 받아 법정동 코드 반환
    @PostMapping("start-query")
    public RGeoResponseDto getBjDongCode(
            @RequestBody
            PointDto point
    ) {
        return service.getBjDongCode(point);
    }

    // 좌표와 법정동 코드도 같이 전달
    @PostMapping("/check")
    public ResponseEntity<?> locateAddress(
            @RequestBody NaviWithQueryDto dto
    ) {
        PointDto pointDto = service.locateAddress(dto);
        if (pointDto == null) {
            return ResponseEntity.badRequest().body("주소로부터 좌표를 얻는 데 실패했습니다.");
        }

        RGeoResponseDto rGeoResponseDto = service.getBjDongCode(pointDto);
        if (rGeoResponseDto == null) {
            return ResponseEntity.badRequest().body("법정동 코드를 조회하는 데 실패했습니다.");
        }

        LocationInfoDto locationInfo = new LocationInfoDto();
        locationInfo.setLat(pointDto.getLat());
        locationInfo.setLng(pointDto.getLng());
        locationInfo.setBjDongCode(rGeoResponseDto.getBjDongCode());

        return ResponseEntity.ok(locationInfo);
    }

}
