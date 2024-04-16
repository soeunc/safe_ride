package com.example.safe_ride.locationInfo.controller;

import com.example.safe_ride.locationInfo.entity.LocationInfo;
import com.example.safe_ride.locationInfo.repo.LocationInfoRepo;
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
import java.util.List;


@Slf4j
@Controller
@RequiredArgsConstructor
public class LocationInfoController {
    private final LocationInfoService locationInfoService;
    private final LocationInfoRepo locationInfoRepo;

    // API 호출 테스트 페이지
    @GetMapping("/public-bicycle-test")
    public String getFirst(Model model) {
        try {
            String lcgvmnInstCd = "3000000000";
            List<String> results1 = locationInfoService.fetchData("inf_101_00010001", lcgvmnInstCd, null, null, "1");
            List<String> results2 = locationInfoService.fetchData("inf_101_00010002", lcgvmnInstCd, null, null, "2");
//            List<String> results3 = locationInfoService.fetchData("inf_101_00010003", lcgvmnInstCd, "20230303", "20230304", "3");

            model.addAttribute("results1", results1);
            model.addAttribute("results2", results2);
//            model.addAttribute("results3", results3);

            return "LocationInfoTest";
        } catch (IOException e) {
            return "error";
        }
    }

    // 메인 페이지
    @GetMapping("/public-bicycle")
    public String showForm(Model model) {
        List<String> sidoList = locationInfoService.getSido();
        model.addAttribute("sidoList", sidoList);
        return "LocationInfo";
    }

    // 동적으로 시군구를 제공
    @ResponseBody
    @GetMapping("/sigungu")
    public List<String> getSigunguBySido(
            @RequestParam("sido")
            String sido
    ) {
        return locationInfoService.getSigunguBySido(sido);
    }

    // 동적으로 읍면동을 제공
    @ResponseBody
    @GetMapping("/eupmyundong")
    public List<String> getEupmyundongBySigungu(
            @RequestParam("sigungu")
            String sigungu
    ) {
        return locationInfoService.getEupmyundongBySigungu(sigungu);
    }

    // 폼 제출 후 메인 페이지
    @PostMapping("/public-bicycle")
    public String searchFormData(
            @RequestParam
            String sido,
            @RequestParam
            String sigungu,
            @RequestParam
            String eupmyundong,
            Model model
    ) throws IOException {
        String lcgvmnInstCd = locationInfoService.getAddressCode(sido);

        System.out.println("시도 : " + sido);
        System.out.println("시군구 : " + sigungu);
        System.out.println("읍면동 : " + eupmyundong);
        System.out.println("lcgvmnInstCd : " + lcgvmnInstCd);
        List<String> results1 = locationInfoService.fetchData("inf_101_00010001", lcgvmnInstCd, null, null, "1");
        List<String> results2 = locationInfoService.fetchData("inf_101_00010002", lcgvmnInstCd, null, null, "2");
        List<String> results3 = locationInfoService.fetchData("inf_101_00010003", lcgvmnInstCd, "20230303", "20230304", "3");

        model.addAttribute("results1", results1);
        model.addAttribute("results2", results2);
//        model.addAttribute("results3", results3);

        return "LocationInfo";
    }

}
