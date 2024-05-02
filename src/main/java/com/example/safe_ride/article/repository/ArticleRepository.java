package com.example.safe_ride.article.repository;

import com.example.safe_ride.article.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findByRegionId(Long regionId);
    Page<Article> findByRegionIdIn(Pageable pageable, List<Long> regionIds);
}
