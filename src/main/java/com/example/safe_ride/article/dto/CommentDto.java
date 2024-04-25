package com.example.safe_ride.article.dto;

import com.example.safe_ride.article.entity.Comment;
import com.example.safe_ride.member.dto.MemberDto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private String content;
    private MemberDto writer;
    private Long articleId;  // 게시글 ID

    public static CommentDto fromEntity(Comment entity) {
        return CommentDto.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .writer(MemberDto.fromEntity(entity.getWriter()))
                .articleId(entity.getArticle().getId())
                .build();
    }

    // setter 메서드 추가
    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }
}
