package com.example.safe_ride.matching.controller;

import com.example.safe_ride.article.entity.Region;
import com.example.safe_ride.article.service.RegionService;
import com.example.safe_ride.matching.dto.MatchingApplicationDto;
import com.example.safe_ride.matching.dto.MatchingDto;
import com.example.safe_ride.matching.entity.MatchingApplication;
import com.example.safe_ride.matching.entity.MatchingStatus;
import com.example.safe_ride.matching.service.MatchingService;
import com.example.safe_ride.member.entity.Member;
import com.example.safe_ride.member.repo.MemberRepo;
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
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/matching")
@RequiredArgsConstructor
public class MatchingController {
    private final MatchingService matchingService;
    private final RegionService regionService;
    private final MemberRepo memberRepository;
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
        List<MatchingApplication> applications = matchingService.getApplicationsByMatchingId(id);
        // 현재 사용자 정보를 모델에 추가
        Member currentUser = getUserEntity();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("matching", matchingDto);
        model.addAttribute("applications", applications);
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

        // 매칭글 작성자와 현재 로그인한 사용자가 같은 경우에만 거절 처리
        if(currentUser.getId().equals(matchingAuthorId)) {
            // 매칭 상태를 수락으로 변경
            matchingDto.setStatus(MatchingStatus.ACCEPTED);
            // 매칭 상태 업데이트
            matchingService.updateMatching(matchingId, matchingDto);
            // 매칭글 신청 ID 가져오기
            matchingService.acceptMatchingApplication(applicationId); // 매칭 신청 ID를 전달하여 거절 처리
        } else {
            // 매칭글 작성자가 아닌 경우에는 권한이 없다는 메시지를 보여줄 수 있습니다.
            // 혹은 다른 처리 방법을 선택할 수도 있습니다.
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
            matchingService.rejectMatchingApplication(applicationId); // 매칭 신청 ID를 전달하여 거절 처리
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
        MatchingApplication application = matchingService.getApplicationById(matchingId, currentUser.getId());
        // 매칭 신청이 존재하고 상태가 PENDING이면 취소 처리
        if (application != null ) {
            matchingService.cancelMatchingApplication(application);
        }
        return "redirect:/matching/" + application.getMatching().getId();
    }
}