package com.example.safe_ride.article.controller;

import com.example.safe_ride.article.entity.Region;
import com.example.safe_ride.article.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    @GetMapping("/city")
    public ResponseEntity<List<Region>> getCitiesByMetropolitanCity(@RequestParam String metropolitanCity) {
        List<Region> city = regionService.getCitiesByMetropolitanCity(metropolitanCity);
        return new ResponseEntity<>(city, HttpStatus.OK);
    }
}