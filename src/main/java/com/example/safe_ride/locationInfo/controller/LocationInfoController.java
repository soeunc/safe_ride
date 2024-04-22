package com.example.safe_ride.locationInfo.controller;

import com.example.safe_ride.locationInfo.service.LocationInfoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;


@Slf4j
@Controller
@RequiredArgsConstructor
public class LocationInfoController {
    private final LocationInfoService locationInfoService;

    // API 호출 테스트 페이지
    @GetMapping("/public-bicycle-test")
    public String getFirst(Model model) {
        try {
            String lcgvmnInstCd = "1100000000";
            List<String> results1 = locationInfoService.fetchDataTest("inf_101_00010001", lcgvmnInstCd, null, null, "1");
            List<String> results2 = locationInfoService.fetchDataTest("inf_101_00010002", lcgvmnInstCd, null, null, "2");

            model.addAttribute("results1", results1);
            model.addAttribute("results2", results2);

            return "/locationInfo/LocationInfoTest";
        } catch (IOException e) {
            return "error";
        }
    }

    // 메인 페이지
    @GetMapping("/public-bicycle")
    public String showForm() {
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
        //test
        String sido = locationInfoService.getSido(roadAddrPart1);

        //real
        String lcgvmnInstCd = locationInfoService.getAddressCode(roadAddrPart1);
        String doroJuso = locationInfoService.getDoroJuso(roadAddrPart1);
        List<String> results1 = locationInfoService.fetchData(doroJuso, "inf_101_00010001","inf_101_00010002", lcgvmnInstCd, null, null);

        //test
        model.addAttribute("sido", sido);
        model.addAttribute("roadFullAddr", roadFullAddr);
        model.addAttribute("roadAddrPart1", roadAddrPart1);

        //real
        model.addAttribute("lcgvmnInstCd", lcgvmnInstCd);
        model.addAttribute("doroJuso", doroJuso);
        model.addAttribute("results1", results1);

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
