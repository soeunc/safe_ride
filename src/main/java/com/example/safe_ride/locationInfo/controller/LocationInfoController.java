package com.example.safe_ride.locationInfo.controller;

import com.example.safe_ride.locationInfo.dto.BicycleInfo;
import com.example.safe_ride.locationInfo.dto.StationAndBicycleInfo;
import com.example.safe_ride.locationInfo.dto.StationInfo;
import com.example.safe_ride.locationInfo.service.LocationInfoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Controller
@RequiredArgsConstructor
public class LocationInfoController {
    private final LocationInfoService locationInfoService;

    // 메인 페이지
    @GetMapping("/public-bicycle")
    public String showForm(Model model) {
        model.addAttribute("combinedInfo", new ArrayList<>());
        return "/locationInfo/LocationInfo";
    }

    // 폼 제출 후 메인 페이지 (도로명)
    @PostMapping("/public-bicycle")
    public String formData(
            @RequestParam
            String roadFullAddr,
            @RequestParam
            String roadAddrPart1,
            Model model
    ) throws IOException {

        String lcgvmnInstCd = locationInfoService.getAddressCode(roadAddrPart1);
        String doroJuso = locationInfoService.getDoroJuso(roadAddrPart1);
        List<StationInfo> stationInfos = locationInfoService.fetchStationData(doroJuso, "inf_101_00010001",lcgvmnInstCd, null, null);
        List<BicycleInfo> bicycleInfos = locationInfoService.fetchStationAndBicycleData("inf_101_00010002",lcgvmnInstCd, null, null);

        List<StationAndBicycleInfo> combinedInfo = new ArrayList<>();
        for (StationInfo station : stationInfos) {
            BicycleInfo bicycle = locationInfoService.findBicycleInfoByStationId(station.getRntstnId(), bicycleInfos);
            combinedInfo.add(new StationAndBicycleInfo(station, bicycle));
        }

        model.addAttribute("doroJuso", doroJuso);
        model.addAttribute("combinedInfo", combinedInfo);

        return "/locationInfo/LocationInfo";
    }

    // 주소 API 호출
    @RequestMapping(value = {"/public-bicycle/jusoPopup"})
    public String jusoPopup(
            HttpServletRequest request,
            Model model
    ) {
        String inputYn = request.getParameter("inputYn");
        String roadFullAddr = request.getParameter("roadFullAddr");
        String roadAddrPart1 = request.getParameter("roadAddrPart1");


        log.debug("inputYn: {}"
                        + ", roadFullAddr: {}"
                        + ", roadAddrPart1: {}"
                , inputYn, roadFullAddr, roadAddrPart1);

        String confmKey = "devU01TX0FVVEgyMDI0MDQxODE3MDEwNzExNDcwMTQ=";

        model.addAttribute("confmKey", confmKey);
        model.addAttribute("inputYn", inputYn);
        model.addAttribute("roadFullAddr", roadFullAddr);
        model.addAttribute("roadAddrPart1", roadAddrPart1);

        return "/locationInfo/jusoPopup";
    }

    // geolocation


}
