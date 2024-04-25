package com.example.safe_ride.article.controller;

import com.example.safe_ride.article.dto.ArticleDto;
import com.example.safe_ride.article.entity.Region;
import com.example.safe_ride.article.service.ArticleService;
import com.example.safe_ride.article.service.CommentService;
import com.example.safe_ride.article.service.RegionService;
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

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/article")
@RequiredArgsConstructor
public class    ArticleController {
    public final ArticleService articleService;
    public final RegionService regionService;
    public final CommentService commentService;
    public final MemberRepo memberRepository;

    // 게시글 페이지 이동
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("articleDto", new ArticleDto());
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
        return "/article/create";
    }

    // 게시글 생성
    @PostMapping("/create")
    public String createArticle(@ModelAttribute("articleDto") ArticleDto articleDto) {
        // 선택된 광역자치구와 도시 정보를 이용하여 ArticleDto 생성
        Long regionId = regionService.getRegionIdByMetropolitanCityAndCity(articleDto.getMetropolitanCity(), articleDto.getCity());
        articleDto.setRegionId(regionId);
        Long newId = articleService.createArticle(articleDto).getId();
        return String.format("redirect:/article/%d", newId);
    }

    // 특정 게시글 조회
    @GetMapping("/{id}")
    public String viewArtilce(@PathVariable Long id, Model model) {
        ArticleDto article = articleService.readOne(id);
        Member currentUser = getUserEntity();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("article", article);
        model.addAttribute("comment", commentService.commentByArticle(id));
        return "/article/view";
    }

    private Member getUserEntity() {
        UserDetails userDetails =
                (UserDetails) (SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        return memberRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public String homePage(@RequestParam(name = "page", defaultValue = "0") int page,
                           @RequestParam(name = "metropolitanCity", required = false) String metropolitanCity,
                           @RequestParam(name = "city", required = false) String city,
                           Model model) {
        // 페이지네이션에 사용할 페이지 크기
        int pageSize = 12;
        Page<ArticleDto> articlePage;

        if (metropolitanCity != null && !metropolitanCity.isEmpty()) {
            if (city != null && !city.isEmpty()) {
                // 광역자치구와 도시에 따라 게시글을 필터링합니다.
                articlePage = articleService.filterArticlesByRegion(PageRequest.of(page, pageSize), metropolitanCity, city);
            } else {
                // 광역자치구에 따라 게시글을 필터링합니다.
                articlePage = articleService.filterArticlesByMetropolitanCity(PageRequest.of(page, pageSize), metropolitanCity);
            }
        } else {
            // 광역자치구가 선택되지 않은 경우 모든 게시글을 조회합니다.
            articlePage = articleService.readPage(PageRequest.of(page, pageSize));
        }

        // 모델에 페이지 정보와 선택된 광역자치구, 도시 정보를 추가합니다.
        model.addAttribute("page", articlePage);
        model.addAttribute("selectedMetropolitanCity", metropolitanCity);
        model.addAttribute("selectedCity", city);

        // 전체 광역자치구 목록을 가져와 모델에 추가합니다.
        List<String> metropolitanCities = regionService.getAllMetropolitanCities();
        model.addAttribute("metropolitanCities", metropolitanCities);

        // 선택된 광역자치구에 따른 도시 목록을 가져와 모델에 추가합니다.
        if (metropolitanCity != null && !metropolitanCity.isEmpty()) {
            List<Region> cities = regionService.getCitiesByMetropolitanCity(metropolitanCity);
            List<String> cityNames = cities.stream().map(Region::getCity).collect(Collectors.toList());
            model.addAttribute("cities", cityNames);
        }

        return "article/home";
    }

    @GetMapping("/{id}/edit")
    public String editPostForm(@PathVariable Long id, Model model) {
        ArticleDto article = articleService.readOne(id);
        model.addAttribute("article", article);
        List<String> metropolitanCities = regionService.getAllMetropolitanCities();
        List<Region> cities = null;
        if (article.getMetropolitanCity() != null) {
            cities = regionService.getCitiesByMetropolitanCity(article.getMetropolitanCity());
        }
        List<String> cityNames = cities.stream().map(Region::getCity).collect(Collectors.toList());
        model.addAttribute("cities", cityNames);
        model.addAttribute("metropolitanCities", metropolitanCities);
        return "/article/edit";
    }

    // 게시글 수정 처리
    @PostMapping("/{id}/edit")
    public String editArticle(@PathVariable Long id, @ModelAttribute("articleDto") ArticleDto dto) {
        // 게시글 ID와 DTO를 사용하여 게시글을 수정합니다.
        articleService.updateArticle(id, dto);


        // 수정된 게시글을 확인할 수 있는 상세 페이지로 리다이렉트합니다.
        return String.format("redirect:/article/%d", id);
    }


    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable Long id) {
        articleService.delete(id);
        return "redirect:/article";
    }

    @GetMapping("/filter")
    @ResponseBody // JSON 형식의 응답을 반환하는 것을 명시
    public List<ArticleDto> filterArticles(@RequestParam String metropolitanCity, @RequestParam String city) {
        return articleService.filterArticlesByRegion(metropolitanCity, city);
    }
}
