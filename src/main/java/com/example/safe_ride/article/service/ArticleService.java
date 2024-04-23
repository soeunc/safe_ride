package com.example.safe_ride.article.service;

import com.example.safe_ride.article.dto.ArticleDto;
import com.example.safe_ride.article.entity.Article;
import com.example.safe_ride.article.entity.Region;
import com.example.safe_ride.article.repository.ArticleRepository;
import com.example.safe_ride.article.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {
    public final ArticleRepository articleRepository;
    public final RegionRepository regionRepository;
    public final RegionService regionService;


    // 게시글 생성
    public ArticleDto createArticle(ArticleDto dto) {
        // 게시글에 선택된 지역 정보 가져오기
        if (dto.getMetropolitanCity() == null || dto.getCity() == null) {
            throw new IllegalArgumentException("Metropolitan city and city must not be null");
        }

        // 광역자치구와 도시에 해당하는 Region ID 가져오기
        Long regionId = regionRepository.findByMetropolitanCityAndCity(dto.getMetropolitanCity(), dto.getCity())
                .map(Region::getId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid metropolitan city or city"));

        // 현재 시간 가져오기
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());

        // 게시글 생성
        Article article = Article.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .hit(dto.getHit())
                .createdAt(currentTime) // 현재 시간을 그대로 저장
                .region(new Region(regionId)) // 광역자치구와 도시에 해당하는 Region ID 설정
                .build();

        Article savedArticle = articleRepository.save(article);

        return ArticleDto.fromEntity(savedArticle);
    }


    // 게시글 상세 조회
    public ArticleDto readOne(Long id) {
        // 게시글 조회
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + id));

        // 조회수가 null인 경우 0으로 초기화
        if (article.getHit() == null) {
            article.setHit(0);
        }

        // 조회수 증가
        article.setHit(article.getHit() + 1);
        articleRepository.save(article); // 변경된 조회수 저장

        // ArticleDto로 변환하여 반환
        return ArticleDto.fromEntity(article);
    }

    // 게시글 전체 조회
    public Page<ArticleDto> readPage(Pageable pageable) {
        Page<Article> articles = articleRepository.findAll(pageable);
        return articles.map(ArticleDto::fromEntity);
    }

    // 게시글 수정
    public ArticleDto updateArticle(Long id, ArticleDto dto) {
        // 해당 ID의 게시글을 조회
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + id));

        // 게시글에 변경된 내용 적용
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setUpdatedAt(new Timestamp(System.currentTimeMillis())); // 수정 시간 업데이트

        // 광역자치구와 도시에 해당하는 Region ID 가져오기
        Long regionId = regionRepository.findByMetropolitanCityAndCity(dto.getMetropolitanCity(), dto.getCity())
                .map(Region::getId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid metropolitan city or city"));

        article.setRegion(new Region(regionId)); // 광역자치구와 도시에 해당하는 Region ID 설정

        // 수정된 게시글 저장
        return ArticleDto.fromEntity(articleRepository.save(article));
    }

    public void delete(Long id) {
        // ID를 기반으로 삭제할 게시글을 데이터베이스에서 조회하고 삭제
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + id));
        articleRepository.delete(article);
    }

    public List<ArticleDto> filterArticlesByRegion(String metropolitanCity, String city) {
        // 선택된 광역자치구와 시군구에 해당하는 Region ID 가져오기
        Long regionId = regionService.getRegionIdByMetropolitanCityAndCity(metropolitanCity, city);

        // 해당 지역에 속하는 게시글을 가져오기
        List<Article> articlesInRegion = articleRepository.findByRegionId(regionId);

        // Article 엔티티를 ArticleDto로 변환하여 반환
        return articlesInRegion.stream()
                .map(ArticleDto::fromEntity)
                .collect(Collectors.toList());
    }

    public Page<ArticleDto> filterArticlesByRegion(Pageable pageable, String metropolitanCity, String city) {
        // 선택된 광역자치구와 도시에 해당하는 Region ID 가져오기
        Long regionId = regionService.getRegionIdByMetropolitanCityAndCity(metropolitanCity, city);

        // 해당 Region ID를 리스트에 담아서 findByRegionIdIn 메서드에 전달
        return articleRepository.findByRegionIdIn(pageable, List.of(regionId))
                .map(ArticleDto::fromEntity);
    }

    public Page<ArticleDto> filterArticlesByMetropolitanCity(Pageable pageable, String metropolitanCity) {
        List<Region> cities = regionService.getCitiesByMetropolitanCity(metropolitanCity);
        List<Long> regionIds = cities.stream().map(Region::getId).collect(Collectors.toList());
        Page<Article> articlePage = articleRepository.findByRegionIdIn(pageable, regionIds);
        return articlePage.map(ArticleDto::fromEntity);
    }

}
