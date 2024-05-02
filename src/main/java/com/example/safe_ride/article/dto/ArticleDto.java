package com.example.safe_ride.article.dto;

import com.example.safe_ride.article.entity.Article;
import com.example.safe_ride.member.dto.MemberDto;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ArticleDto {
    private Long id;
    private String title;
    private String content;
    private Integer hit;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String metropolitanCity; // 광역자치구
    private String city; // 도시
    private Long regionId; // Region 객체 대신 ID만 사용
    private MemberDto member; // 작성자 정보를 MemberDto로 변경

    public static ArticleDto fromEntity(Article entity) {
        return ArticleDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .hit(entity.getHit())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .metropolitanCity(entity.getRegion().getMetropolitanCity())
                .city(entity.getRegion().getCity())
                .regionId(entity.getRegion().getId()) // Region 객체의 ID 사용
                .member(MemberDto.fromEntity(entity.getMember())) // MemberDto로 변경
                .build();
    }

    // setter 메서드 추가
    public void setMetropolitanCity(String metropolitanCity) {
        this.metropolitanCity = metropolitanCity;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
