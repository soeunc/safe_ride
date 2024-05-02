package com.example.safe_ride.article.service;

import com.example.safe_ride.article.dto.CommentDto;
import com.example.safe_ride.article.entity.Article;
import com.example.safe_ride.article.entity.Comment;
import com.example.safe_ride.article.repository.ArticleRepository;
import com.example.safe_ride.article.repository.CommentRepository;
import com.example.safe_ride.member.entity.Member;
import com.example.safe_ride.member.repo.MemberRepo;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@Builder
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final MemberRepo memberRepository;

    // 댓글 생성
    public void createComment(Long articleId, CommentDto dto) {
        Member currentUser = getUserEntity();

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article with ID " + articleId + " not found"));

        // 현재 시간 가져오기

        Comment comment = Comment.builder()
                .content(dto.getContent())
                .writer(currentUser)
                .article(article)
                .build();

        commentRepository.save(comment);
    }

    // 댓글 삭제
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment with ID " + commentId + " not found"));

        commentRepository.delete(comment);
    }

    // 현재 사용자 불러오기
    private Member getUserEntity() {
        UserDetails userDetails =
                (UserDetails) (SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        return memberRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public List<Comment> commentByArticle(Long articleId) {
        return commentRepository.findByArticleId(articleId);
    }
}
