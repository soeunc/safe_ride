package com.example.safe_ride.matching.controller;

import com.example.safe_ride.article.entity.Region;
import com.example.safe_ride.article.repository.RegionRepository;
import com.example.safe_ride.article.service.RegionService;
import com.example.safe_ride.matching.dto.MatchingApplicationDto;
import com.example.safe_ride.matching.dto.MatchingDto;
import com.example.safe_ride.matching.entity.MatchingApplication;
import com.example.safe_ride.matching.entity.MatchingStatus;
import com.example.safe_ride.matching.service.MannerService;
import com.example.safe_ride.matching.service.MatchingApplicationService;
import com.example.safe_ride.matching.service.MatchingService;
import com.example.safe_ride.member.entity.Member;
import com.example.safe_ride.member.repo.MemberRepo;
import com.example.safe_ride.myPage.dto.MyPageDto;
import com.example.safe_ride.myPage.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/matching")
@RequiredArgsConstructor
public class MatchingController {
    private final MatchingService matchingService;
    private final RegionService regionService;
    private final MemberRepo memberRepository;
    private final MatchingApplicationService matchingApplicationService;
    private final RegionRepository regionRepository;
    private final MannerService mannerService;
    private final MyPageService myPageService;
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
    public String createMatching(@ModelAttribute MatchingDto matchingDto, @RequestParam("ridingTime") String ridingTimeString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime ridingTime = LocalDateTime.parse(ridingTimeString, formatter);
        matchingDto.setRidingTime(ridingTime);

        Long regionId = regionService.getRegionIdByMetropolitanCityAndCity(matchingDto.getMetropolitanCity(), matchingDto.getCity());
        matchingDto.setRegionId(regionId);
        Long newId = matchingService.createMatching(matchingDto).getId();
        return String.format("redirect:/matching/%d", newId);
    }

    // 매칭 상세 조회 페이지
    @GetMapping("/{id}")
    public String viewMatching(@PathVariable Long id, Model model) {
        MatchingDto matchingDto = matchingService.getMatchingById(id);
        List<MatchingApplication> applications = matchingApplicationService.getPendingApplicationsByMatchingId(id);
        // 현재 사용자 정보를 모델에 추가
        Member currentUser = getUserEntity();
        //라이딩 정보 가져오기
        MyPageDto myPageDto = myPageService.readRidingRecord(currentUser.getId());
        //뱃지 가져오기
        model.addAttribute("badges", myPageService.readBadges(currentUser.getId(), myPageDto.getTotalRecord()));
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("matching", matchingDto);
        model.addAttribute("applications", applications);

        // 매칭 엔티티에서 regionId 가져오기
        Long regionId = matchingDto.getRegionId();
        // regionId를 사용하여 Region 엔티티 조회
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new IllegalArgumentException("Region not found with id: " + regionId));
        // Metropolitan city와 city 값을 모델에 추가
        model.addAttribute("metropolitanCity", region.getMetropolitanCity());
        model.addAttribute("city", region.getCity());

        return "matching/view";
    }


    @GetMapping("/list")
    public String homePage(@RequestParam(name = "page", defaultValue = "0") int page,
                           @RequestParam(name = "metropolitanCity", required = false) String metropolitanCity,
                           @RequestParam(name = "city", required = false) String city, // 추가: 도시 파라미터
                           Model model) {
        // 페이지네이션에 사용할 페이지 크기
        int pageSize = 12;
        Page<MatchingDto> matchingDtoPage;

        if (metropolitanCity != null && !metropolitanCity.isEmpty()) {
            List<Region> cities = regionService.getCitiesByMetropolitanCity(metropolitanCity);
            List<String> cityNames = cities.stream().map(Region::getCity).collect(Collectors.toList());
            model.addAttribute("cities", cityNames);
            if (city != null && !city.isEmpty()) {
                matchingDtoPage = matchingService.readPageByMetropolitanCityAndCity(PageRequest.of(page, pageSize), metropolitanCity, city); // 수정: 도시 파라미터 추가
            } else {
                matchingDtoPage = matchingService.readPageByMetropolitanCity(PageRequest.of(page, pageSize), metropolitanCity);
            }
        } else {
            // 광역자치구가 선택되지 않은 경우 모든 매칭글 조회
            matchingDtoPage = matchingService.readPage(PageRequest.of(page, pageSize));
        }

        model.addAttribute("page", matchingDtoPage);
        model.addAttribute("selectedMetropolitanCity", metropolitanCity);

        // 전체 광역자치구 목록을 가져와 모델에 추가
        List<String> metropolitanCities = regionService.getAllMetropolitanCities();
        model.addAttribute("metropolitanCities", metropolitanCities);

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
        return "matching/edit";
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
        matchingApplicationService.applyForMatching(id, applicationDto);
        return "redirect:/matching/" + id; //
    }

    // 매칭 신청 수락
    @PostMapping("/{matchingId}/accept/{applicationId}")
    public String acceptMatchingApplication(
            @PathVariable Long matchingId,
            @PathVariable Long applicationId) throws AccessDeniedException {
        // 현재 로그인한 사용자 정보 가져오기
        Member currentUser = getUserEntity();
        // 해당 매칭의 작성자 정보 가져오기
        MatchingDto matchingDto = matchingService.getMatchingById(matchingId);
        Long matchingAuthorId = matchingDto.getMember().getId();

        // 매칭글 작성자와 현재 로그인한 사용자가 같은 경우에만 처리
        if(currentUser.getId().equals(matchingAuthorId)) {
            // 매칭 상태를 수락으로 변경
            matchingService.updateMatchingStatus(matchingId, MatchingStatus.ACCEPTED);
            // 매칭글 신청 수락 처리
            matchingApplicationService.acceptMatchingApplication(applicationId);
        } else {
            // 매칭글 작성자가 아닌 경우에는 권한이 없다는 메시지를 보여줄 수 있습니다.
            throw new AccessDeniedException("매칭글 작성자만 수락 할 수 있습니다.");
        }
        return "redirect:/matching/" + matchingId;
    }


    // 매칭 신청 거절
    @PostMapping("/{matchingId}/reject/{applicationId}")
    public String rejectMatchingApplication(
            @PathVariable Long matchingId,
            @PathVariable Long applicationId) throws AccessDeniedException {
        // 현재 로그인한 사용자 정보 가져오기
        Member currentUser = getUserEntity();
        // 해당 매칭의 작성자 정보 가져오기
        MatchingDto matchingDto = matchingService.getMatchingById(matchingId);
        Long matchingAuthorId = matchingDto.getMember().getId();

        // 매칭글 작성자와 현재 로그인한 사용자가 같은 경우에만 거절 처리
        if(currentUser.getId().equals(matchingAuthorId)) {
            // 매칭글 신청 ID 가져오기
            matchingApplicationService.rejectMatchingApplication(applicationId); // 매칭 신청 ID를 전달하여 거절 처리
        } else {
            // 매칭글 작성자가 아닌 경우에는 권한이 없다는 메시지를 보여줄 수 있습니다.
            // 혹은 다른 처리 방법을 선택할 수도 있습니다.
            throw new AccessDeniedException("매칭글 작성자만 거절 할 수 있습니다.");
        }
        return "redirect:/matching/" + matchingId;
    }


    private Member getUserEntity() {
        UserDetails userDetails =
                (UserDetails) (SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        return memberRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    // 매칭 신청 취소 처리
    @PostMapping("/{matchingId}/cancel-application")
    public String cancelMatchingApplication(@PathVariable Long matchingId) {
        // 현재 사용자 정보 가져오기
        Member currentUser = getUserEntity();
        // 매칭 신청 조회
        MatchingApplication application = matchingApplicationService.getApplicationById(matchingId, currentUser.getId());
        // 매칭 신청이 존재하고 상태가 PENDING이면 취소 처리
        if (application != null ) {
            matchingApplicationService.cancelMatchingApplication(application);
        }
        return "redirect:/matching/" + application.getMatching().getId();
    }

    // 매너 평가하기
    @PostMapping("/{matchingId}/manner")
    public String rateManner(@PathVariable Long matchingId,
                             @RequestParam int score,
                             @RequestParam String comment) {
        mannerService.rateManner(matchingId, score, comment);
        return "redirect:/matching/" + matchingId;
    }

    // 매칭 상태를 END로 변경
    @PostMapping("/{id}/end")
    public String endMatching(@PathVariable Long id) {
        matchingService.endMatching(id);
        return "redirect:/matching/" + id;
    }

}