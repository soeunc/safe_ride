package com.example.safe_ride.locationInfo.controller;

import com.example.safe_ride.locationInfo.dto.BicycleInfo;
import com.example.safe_ride.locationInfo.dto.StationAndBicycleInfo;
import com.example.safe_ride.locationInfo.dto.StationInfo;
import com.example.safe_ride.locationInfo.service.LocationInfoService;
import com.example.safe_ride.safe.dto.PointDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Slf4j
@Controller
@RequiredArgsConstructor
public class LocationInfoController {
    private final LocationInfoService locationInfoService;


    // 세션에 저장되어 있는 값을 검사한 후 model에 전달
    // 타입 캐스트 오류 예방함
    private <T> void safelyAddDataToSession(
            HttpSession session,
            Model model,
            String sessionKey,
            Class<T> type
    ) {
        Object data = session.getAttribute(sessionKey);
        if (data instanceof List<?>) {
            List<?> list = (List<?>) data;
            if (!list.isEmpty() && type.isInstance(list.get(0))) {
                model.addAttribute(sessionKey, data);
            } else {
                model.addAttribute(sessionKey, new ArrayList<>());
            }
        } else {
            model.addAttribute(sessionKey, new ArrayList<>());
        }
    }

    // 메인 페이지
    // 타입 검사 로직을 들고와 뷰로 반환
    @GetMapping("/public-bicycle")
    public String display(Model model, HttpSession session) {
        Object combinedInfo = session.getAttribute("combinedInfo");
        // 검색이 수행되었는지 판단하여 isSearched 설정
        if (combinedInfo instanceof List) {
            model.addAttribute("isSearched", true);
        } else {
            model.addAttribute("isSearched", false);
        }
        // 모든 세션 데이터를 모델에 안전하게 추가
        safelyAddDataToSession(session, model, "combinedInfo", StationAndBicycleInfo.class);
        safelyAddDataToSession(session, model, "stationInfos", StationInfo.class);
        safelyAddDataToSession(session, model, "bicycleInfos", BicycleInfo.class);
        safelyAddDataToSession(session, model, "selectedInfo", StationAndBicycleInfo.class);
        safelyAddDataToSession(session, model, "targetStationId", String.class);

        return "/locationInfo/LocationInfo";
    }

    // 위치 제출 후 메인 페이지 (위도 경도)
    @PostMapping("/public-bicycle/point")
    public String pointData(
    ) throws IOException {


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

        log.info("여기는 상세보기 Combined Info Size: {}", combinedInfo.size());
        combinedInfo.forEach(info -> log.info("Station ID: {}", info.getStationInfo().getRntstnId()));
        log.info("URL에서 받은 stationId: '{}'", stationId);

        StationAndBicycleInfo selectedInfo = combinedInfo.stream()
                .filter(info -> stationId.equals(info.getStationInfo().getRntstnId()))
                .findFirst()
                .orElse(null);

        if (selectedInfo != null) {
            log.info("selectedInfo의 rntstnId: " + selectedInfo.getStationInfo().getRntstnId());
            model.addAttribute("targetStationId", selectedInfo.getStationInfo().getRntstnId());
            model.addAttribute("selectedInfo", selectedInfo.getStationInfo());
        } else {
            log.info("세션에 selectedInfo가 존재하지 않습니다.");
            model.addAttribute("message", "해당 ID의 대여소 정보가 존재하지 않습니다.");
        }

        String targetStationId = selectedInfo.getStationInfo().getRntstnId();

        log.info("target station Id : " + targetStationId);
        session.setAttribute("targetStationId", targetStationId);
        session.setAttribute("selectedInfo", selectedInfo.getStationInfo());
        model.addAttribute("selectedInfo", selectedInfo.getStationInfo());
        model.addAttribute("targetStationId", targetStationId);
        return "/locationInfo/stationDetailPopup";
    }


    // 도로명주소주소 Popup API 호출
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

    // 도로명주소를 받아와서 세션에 저장
    @PostMapping("/public-bicycle/address")
    public String formData(
            @RequestParam
            String roadAddrPart1,
            HttpSession session
    ) throws IOException {
        // 기존 세션 데이터 삭제
        session.removeAttribute("combinedInfo");
        String lcgvmnInstCd = locationInfoService.getAddressCode(roadAddrPart1);
        String doroJuso = locationInfoService.getDoroJuso(roadAddrPart1);
        List<StationInfo> stationInfos = locationInfoService.fetchStationData(doroJuso, "inf_101_00010001", lcgvmnInstCd, null, null);
        List<BicycleInfo> bicycleInfos = locationInfoService.fetchStationAndBicycleData("inf_101_00010002", lcgvmnInstCd, null, null);

        List<StationAndBicycleInfo> combinedInfo = new ArrayList<>();
        for (StationInfo station : stationInfos) {
            BicycleInfo bicycle = locationInfoService.findBicycleInfoByStationId(station.getRntstnId(), bicycleInfos);
            combinedInfo.add(new StationAndBicycleInfo(station, bicycle));
        }

        log.info("Combined Info Size: {}", combinedInfo.size());
        combinedInfo.forEach(info -> log.info("Station ID: {}", info.getStationInfo().getRntstnId()));
        combinedInfo.forEach(info -> log.info("Count Bicycle: {}", info.getBicycleInfo().getBcyclTpkctNocs()));

        session.setAttribute("lcgvmnInstCd", lcgvmnInstCd);
        session.setAttribute("doroJuso", doroJuso);
        session.setAttribute("stationInfos", stationInfos);
        session.setAttribute("bicycleInfos", bicycleInfos);
        session.setAttribute("combinedInfo", combinedInfo);

        return "redirect:/public-bicycle";
    }

    // 위도 경도 가져와서 모든 데이터를 세션에 저장
    @PostMapping("/public-bicycle/getUserPosition")
    public ResponseEntity<?> receiveUserLocation(
            @RequestBody
            Map<String, Double> payload,
            HttpSession session
    ) throws IOException {
        double lat = payload.get("lat");
        double lot = payload.get("lot");
        log.info("경도 : " + lat);
        log.info("위도 : " + lot);

        PointDto point = new PointDto(lat, lot);
        String lcgvmnInstCd = locationInfoService.setTransBjDongCode(point).getBjDongCode();
        List<StationInfo> stationInfos = locationInfoService.fetchPointData(lat, lot, "inf_101_00010001", lcgvmnInstCd, null, null);
        List<BicycleInfo> bicycleInfos = locationInfoService.fetchStationAndBicycleData("inf_101_00010002",lcgvmnInstCd, null, null);

        List<StationAndBicycleInfo> combinedInfo = new ArrayList<>();
        for (StationInfo station : stationInfos) {
            BicycleInfo bicycle = locationInfoService.findBicycleInfoByStationId(station.getRntstnId(), bicycleInfos);
            combinedInfo.add(new StationAndBicycleInfo(station, bicycle));
        }

        session.setAttribute("combinedInfo", combinedInfo);
        session.setAttribute("isSearched", true);

        // 응답으로 JSON 객체 반환
        return ResponseEntity.ok().body(Map.of(
                "message", "데이터 처리 완료",
                "redirectUrl", "/public-bicycle"
        ));
    }


}
