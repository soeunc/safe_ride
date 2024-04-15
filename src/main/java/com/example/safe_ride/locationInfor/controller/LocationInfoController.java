package com.example.safe_ride.locationInfor.controller;

import com.example.safe_ride.locationInfor.service.LocationInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.List;


@Slf4j
@Controller
@RequiredArgsConstructor
public class LocationInfoController {
    private final LocationInfoService locationInfoService;

    @GetMapping("/public-bicycle")
    public String getFirst(Model model) {
        try {
            String lcgvmnInstCd = "1100000000";
            List<String> results1 = locationInfoService.fetchData("inf_101_00010001", lcgvmnInstCd, null, null, "1");
            List<String> results2 = locationInfoService.fetchData("inf_101_00010002", lcgvmnInstCd, null, null, "2");
            List<String> results3 = locationInfoService.fetchData("inf_101_00010003", lcgvmnInstCd, "20230303", "20230304", "3");

            model.addAttribute("results1", results1);
            model.addAttribute("results2", results2);
            model.addAttribute("results3", results3);
            return "LocationInfo";
        } catch (IOException e) {

            return "error";
        }
    }

//    @PostMapping("/public-bicycle/search")
//    public ModelAndView search (
//            @RequestParam
//            String si_do,
//            @RequestParam
//            String si_gun_gu,
//            @RequestParam
//            String eop_myun_dong
//    ) {
////        String code = itemService.findCodeByItem(si_do, si_gun_gu, eop_myun_dong);
////        ModelAndView mv = new ModelAndView("test2");
////        mv.addObject("code", code);
////        return mv;
//    }
}
