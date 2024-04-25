package com.example.safe_ride.article.controller;

import com.example.safe_ride.article.dto.CommentDto;
import com.example.safe_ride.article.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/article/{articleId}/comment")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    @PostMapping
    public String createComment(
            @PathVariable("articleId")
            Long articleId,
            @ModelAttribute CommentDto dto
            ) {
        commentService.createComment(articleId, dto);
        return String.format("redirect:/article/%d", articleId);
    }

    @PostMapping("/{commentId}/delete")
    public String deleteComment(
            @PathVariable("articleId") Long articleId,
            @PathVariable("commentId") Long commentId
    ) {
        commentService.deleteComment(commentId);
        return "redirect:/article/" + articleId;
    }


}
