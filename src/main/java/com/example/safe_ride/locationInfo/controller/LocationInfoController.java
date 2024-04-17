package com.example.safe_ride.locationInfo.controller;

import com.example.safe_ride.locationInfo.repo.LocationInfoRepo;
import com.example.safe_ride.locationInfo.service.AddressService;
import com.example.safe_ride.locationInfo.service.LocationInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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


    // 폼 제출 후 메인 페이지
    @PostMapping("/public-bicycle")
    @ResponseBody
    public Map<String, List<String>> searchFormData(
            @RequestParam
            String sido,
            @RequestParam
            String sigungu,
            @RequestParam
            String eupmyundong
    ) throws IOException {
        String lcgvmnInstCd = addressService.getAddressCode(sido);
        List<String> results1 = locationInfoService.fetchData(sido, sigungu, eupmyundong, "inf_101_00010001", lcgvmnInstCd, null, null, "1");
        List<String> results2 = locationInfoService.fetchData(sido, sigungu, eupmyundong,"inf_101_00010002", lcgvmnInstCd, null, null, "2");

        Map<String, List<String>> results = new HashMap<>();
        results.put("results1", results1);
        results.put("results2", results2);

        return results;
    }

}
