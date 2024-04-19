package com.example.safe_ride.locationInfo.controller;

import com.example.safe_ride.locationInfo.repo.LocationInfoRepo;
import com.example.safe_ride.locationInfo.service.AddressService;
import com.example.safe_ride.locationInfo.service.LocationInfoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Controller
@RequiredArgsConstructor
public class LocationInfoController {
    private final LocationInfoService locationInfoService;
    private final AddressService addressService;

    // API 호출 테스트 페이지
    @GetMapping("/public-bicycle-test")
    public String getFirst(Model model) {
        try {
            String lcgvmnInstCd = "1100000000";
            List<String> results1 = locationInfoService.fetchDataTest("inf_101_00010001", lcgvmnInstCd, null, null, "1");
            List<String> results2 = locationInfoService.fetchDataTest("inf_101_00010002", lcgvmnInstCd, null, null, "2");
//            List<String> results3 = locationInfoService.fetchData("inf_101_00010003", lcgvmnInstCd, "20230303", "20230304", "3");

            model.addAttribute("results1", results1);
            model.addAttribute("results2", results2);

            return "/locationInfo/LocationInfoTest";
        } catch (IOException e) {
            return "error";
        }
    }

    // 메인 페이지
    @GetMapping("/public-bicycle")
    public String showForm(Model model) {
        List<String> sidoList = addressService.getSido();
        model.addAttribute("sidoList", sidoList);
        return "/locationInfo/LocationInfo";
    }


    // 폼 제출 후 메인 페이지 (도로명)
    @PostMapping("/public-bicycle")
    public String testFormData(
            @RequestParam String roadFullAddr,
            @RequestParam String roadAddrPart1,
            Model model
    ) {
        String doroJuso = addressService.getDoroJuso(roadAddrPart1);

        model.addAttribute("roadFullAddr", roadFullAddr);
        model.addAttribute("roadAddrPart1", roadAddrPart1);
        model.addAttribute("doroJuso", doroJuso);

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

}
