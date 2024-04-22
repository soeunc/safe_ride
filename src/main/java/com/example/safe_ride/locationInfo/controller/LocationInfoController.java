package com.example.safe_ride.locationInfo.controller;

import com.example.safe_ride.locationInfo.dto.BicycleInfo;
import com.example.safe_ride.locationInfo.dto.StationAndBicycleInfo;
import com.example.safe_ride.locationInfo.dto.StationInfo;
import com.example.safe_ride.locationInfo.service.LocationInfoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
        model.addAttribute("isSearched", false);
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
            Model model,
            HttpSession session
    ) throws IOException {
        // 기존 세션 데이터 삭제
        session.removeAttribute("combinedInfo");

        String lcgvmnInstCd = locationInfoService.getAddressCode(roadAddrPart1);
        String doroJuso = locationInfoService.getDoroJuso(roadAddrPart1);
        List<StationInfo> stationInfos = locationInfoService.fetchStationData(doroJuso, "inf_101_00010001",lcgvmnInstCd, null, null);
        List<BicycleInfo> bicycleInfos = locationInfoService.fetchStationAndBicycleData("inf_101_00010002",lcgvmnInstCd, null, null);

        List<StationAndBicycleInfo> combinedInfo = new ArrayList<>();
        for (StationInfo station : stationInfos) {
            BicycleInfo bicycle = locationInfoService.findBicycleInfoByStationId(station.getRntstnId(), bicycleInfos);
            combinedInfo.add(new StationAndBicycleInfo(station, bicycle));
        }



        session.setAttribute("combinedInfo", combinedInfo);
        model.addAttribute("isSearched", true);
        model.addAttribute("doroJuso", doroJuso);
        model.addAttribute("combinedInfo", combinedInfo);

        return "/locationInfo/LocationInfo";
    }

    // 대여소 상세보기 팝업 페이지
    @GetMapping("/public-bicycle/{id}")
    public String stationDetails(
            @PathVariable("id")
            String stationId,
            Model model,
            HttpSession session
    ) {
        List<StationAndBicycleInfo> combinedInfo = (List<StationAndBicycleInfo>) session.getAttribute("combinedInfo");

        StationAndBicycleInfo selectedInfo = combinedInfo.stream()
                .filter(info -> stationId.equals(info.getStationInfo().getRntstnId()))
                .findFirst()
                .orElse(null);

        if (selectedInfo != null)  log.info("selectedInfo의 rntstnId: " + selectedInfo.getStationInfo().getRntstnId());
        else log.info("세션에 selectedInfo가 존재하지 않습니다.");

        String targetStationId = selectedInfo.getStationInfo().getRntstnId();

        log.info("target station Id : " + targetStationId );
        model.addAttribute("targetStationId", targetStationId);
        model.addAttribute("selectedInfo", selectedInfo.getStationInfo());
        return "/locationInfo/stationDetailPopup";
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
