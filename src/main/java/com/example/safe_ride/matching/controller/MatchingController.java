package com.example.safe_ride.matching.controller;

import com.example.safe_ride.article.entity.Region;
import com.example.safe_ride.article.service.RegionService;
import com.example.safe_ride.matching.dto.MatchingApplicationDto;
import com.example.safe_ride.matching.dto.MatchingDto;
import com.example.safe_ride.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/matching")
@RequiredArgsConstructor
public class MatchingController {
    private final MatchingService matchingService;
    private final RegionService regionService;

    // 매칭글 생성 페이지 이동
    @GetMapping("/create")
    public String createMatchingForm(Model model) {
        model.addAttribute("matchingDto", new MatchingDto());
        // 기본적으로 첫 번째 광역자치구에 해당하는 시군구 목록을 가져옴
        List<String> metropolitanCities = regionService.getAllMetropolitanCities();
        List<Region> cities = null;
        if (!metropolitanCities.isEmpty()) {
            cities = regionService.getCitiesByMetropolitanCity(metropolitanCities.get(0));
        }
        // 도시 리스트를 문자열로 변환하여 추가하는 대신, 도시 객체에서 도시 이름을 추출하여 추가합니다.
        List<String> cityNames = cities.stream().map(Region::getCity).collect(Collectors.toList());
        model.addAttribute("city", cityNames);
        model.addAttribute("metropolitanCities", metropolitanCities);
        return "matching/create";
    }

    // 매칭글 생성 처리
    @PostMapping("/create")
    public String createMatching(@ModelAttribute MatchingDto matchingDto) {
        Long regionId = regionService.getRegionIdByMetropolitanCityAndCity(matchingDto.getMetropolitanCity(), matchingDto.getCity());
        matchingDto.setRegionId(regionId);
        Long newId = matchingService.createMatching(matchingDto).getId();
        return String.format("redirect:/matching/%d", newId);
    }

    // 매칭글 상세 조회 페이지
    @GetMapping("/{id}")
    public String viewMatching(@PathVariable Long id, Model model) {
        MatchingDto matchingDto = matchingService.getMatchingById(id);
        model.addAttribute("matching", matchingDto);
        return "matching/view";
    }

    // 모든 매칭글 조회 페이지
    @GetMapping("/list")
    public String homePage(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {
        // 페이지네이션에 사용할 페이지 크기
        int pageSize = 12;
        Page<MatchingDto> matchingDtoPage = matchingService.readPage(PageRequest.of(page, pageSize));
        model.addAttribute("page", matchingDtoPage);

        return "matching/matchingList";
    }

    // 매칭글 수정
    @GetMapping("/{id}/edit")
    public String editMatchingForm(@PathVariable Long id, Model model) {
        MatchingDto matching = matchingService.getMatchingById(id);
        model.addAttribute("matching", matching);
        List<String> metropolitanCities = regionService.getAllMetropolitanCities();
        List<Region> cities = null;
        if (matching.getMetropolitanCity() != null) {
            cities = regionService.getCitiesByMetropolitanCity(matching.getMetropolitanCity());
        }
        List<String> cityNames = cities.stream().map(Region::getCity).collect(Collectors.toList());
        model.addAttribute("cities", cityNames);
        model.addAttribute("metropolitanCities", metropolitanCities);
        return "/matching/edit";
    }

    // 게시글 수정 처리
    @PostMapping("/{id}/edit")
    public String editMatching(@PathVariable Long id, @ModelAttribute("MatchingDto") MatchingDto dto) {
        // 게시글 ID와 DTO를 사용하여 게시글을 수정합니다.
        matchingService.updateMatching(id, dto);


        // 수정된 게시글을 확인할 수 있는 상세 페이지로 리다이렉트합니다.
        return String.format("redirect:/matching/%d", id);
    }

    // 매칭글 삭제 처리
    @PostMapping("/{id}/delete")
    public String deleteMatching(@PathVariable Long id) {
        matchingService.deleteMatching(id);
        return "redirect:/matching/list";
    }

    // 매칭 신청 처리
    @PostMapping("/{id}/apply")
    public String applyForMatching(@PathVariable Long id, @ModelAttribute MatchingApplicationDto applicationDto) {
        matchingService.applyForMatching(id, applicationDto);
        return "redirect:/matching/" + id; //
    }
}