package com.example.safe_ride.safe;

import com.example.safe_ride.safe.dto.PointDto;
import com.example.safe_ride.safe.dto.SafetyDirectionDto;
import com.example.safe_ride.safe.service.SafetyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("safety-direction")
@RequiredArgsConstructor
public class SafetyController {
    private final SafetyService service;

    @PostMapping("/check")
    public PointDto locateAddress(
            @RequestBody SafetyDirectionDto dto
            ) {
        return service.locateAddress(dto);
    }

}
