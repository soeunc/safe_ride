package com.example.safe_ride.locationInfo.controller;

import com.example.safe_ride.locationInfo.dto.LocationInfoResponseDto;
import com.example.safe_ride.locationInfo.dto.TotalInfoDto;
import com.example.safe_ride.locationInfo.entity.TempCombinedInfo;
import com.example.safe_ride.locationInfo.repo.TempCombinedInfoRepo;
import com.example.safe_ride.locationInfo.service.LocationInfoService;
import com.example.safe_ride.locationInfo.service.TempTableService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/public-bicycle")
@RequiredArgsConstructor
public class LocationViewController {
    private final LocationInfoService locationInfoService;
    private final TempCombinedInfoRepo tempCombinedInfoRepo;
    private final TempTableService tempTableService;

    @Value("${juso.api.key}")
    String confmKey;
    // 메인페이지
    @GetMapping
    public String mainPage (
            HttpServletRequest request,
            Model model,
            @PageableDefault(size = 10)
            Pageable pageable,
            @RequestParam(value = "page", defaultValue = "0")
            int page,
            @RequestParam(value = "sort", defaultValue = "distance")
            String sort,
            @RequestParam(value = "direction", defaultValue = "asc")
            String direction
    ) {
        HttpSession session = request.getSession();
        // 다른페이지에서 왔을 경우 초기화 로직 수행
        String referrer = request.getHeader("Referer");
        if (referrer == null || !referrer.contains("/public-bicycle")) {
            session.removeAttribute("isSearched");
            session.removeAttribute("searchInfo");
            tempTableService.clearData();
        }

        LocationInfoResponseDto infoResponse = (LocationInfoResponseDto) session.getAttribute("infoResponse");
        if (infoResponse == null) {
            infoResponse = new LocationInfoResponseDto();
        }

        Boolean isSearched = (Boolean) session.getAttribute("isSearched");
        if (isSearched == null) {
            isSearched = false;
            session.setAttribute("isSearched", false);
        }
        log.info("isSearched = {} ", session.getAttribute("isSearched"));

        // 정렬기능 (거리, 자전거대수)
        Sort.Direction sortDirection;
        if ("bcyclTpkctNocs".equals(sort)) {
            sortDirection = Sort.Direction.DESC; // '보유 자전거수'일 때 내림차순
        } else {
            sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC; // 그 외는 사용자 지정
        }
        // 페이지 처리를 위한 Pageable
        Pageable updatedPageable = PageRequest.of(page, 10, Sort.by(sortDirection, sort));

        Page<TempCombinedInfo> pageInfo = locationInfoService.readTempCombinedInfo(updatedPageable);
        if (pageInfo == null) {
            pageInfo = new PageImpl<>(new ArrayList<>());
        }

        // 지도 처리를 위한 전체 객체
        List<TempCombinedInfo> allStations = tempCombinedInfoRepo.findAll();

        // Total Info 계산
        TotalInfoDto totalInfoDto = locationInfoService.getTotalInfo();
        if (totalInfoDto != null) {
            model.addAttribute("totalInfoDto", totalInfoDto);
        }

        model.addAttribute("sort", sort);
        model.addAttribute("page", pageInfo);
        model.addAttribute("isSearched", isSearched);
        model.addAttribute("infoResponse", infoResponse);
        model.addAttribute("stations", allStations);
        model.addAttribute("totalInfo", totalInfoDto); // 전체 사용 가능 자전거 수
        return "locationInfo/LocationInfo";
    }


    // 도로명주소 Popup API
    @RequestMapping(value = {"/jusoPopup"})
    public String jusoPopup(
            HttpServletRequest request,
            HttpServletResponse response,
            Model model
    ) {
        String inputYn = request.getParameter("inputYn");
        String roadFullAddr = request.getParameter("roadFullAddr");
        String roadAddrPart1 = request.getParameter("roadAddrPart1");

//        // 쿠키 문자열 직접 생성 및 설정
//        String cookieValue = "sessionId=sessionValue; Secure; HttpOnly; SameSite=None";
//        response.addHeader("Set-Cookie", cookieValue);

        log.debug("inputYn: {} roadFullAddr: {} roadAddrPart1: {}", inputYn, roadFullAddr, roadAddrPart1);

        model.addAttribute("confmKey", confmKey);
        model.addAttribute("inputYn", inputYn);
        model.addAttribute("roadFullAddr", roadFullAddr);
        model.addAttribute("roadAddrPart1", roadAddrPart1);

        return "locationInfo/jusoPopup";
    }

    // 상세보기 Popup
    @GetMapping("/detail/{id}")
    public String stationDetails (
            @PathVariable ("id")
            String stationId,
            Model model
    ) {
        TempCombinedInfo selectedInfo = tempCombinedInfoRepo.findByRntstnId(stationId);
        model.addAttribute("selectedInfo", selectedInfo);
        return "locationInfo/stationDetailPopup";
    }

    // 현재 위치 검색 동의 팝업 페이지
    @GetMapping("/positionPopup")
    public String positionPopup() {
        return "locationInfo/positionPopup";
    }
}
