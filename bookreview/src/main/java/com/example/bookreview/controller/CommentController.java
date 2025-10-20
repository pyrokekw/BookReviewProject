package com.example.bookreview.controller;

import com.example.bookreview.dto.CommentCreateDto;
import com.example.bookreview.dto.CommentDto;
import com.example.bookreview.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/comments/add")
    public String addComment(@RequestParam Long reviewId,
                             @RequestParam String text,
                             Principal principal) {
        // Создаем DTO
        CommentCreateDto commentDto = new CommentCreateDto();
        commentDto.setReviewId(reviewId);
        commentDto.setText(text);

        commentService.addComment(commentDto, principal.getName());

        Long bookId = commentService.getBookIdByReviewId(reviewId);
        return "redirect:/books/" + bookId + "#review-" + reviewId;
    }


    @PostMapping("/comments/{id}/delete")
    public String deleteComment(@PathVariable Long id,
                                Principal principal,
                                HttpServletRequest request) {
        try {
            String username = principal.getName();

            // СНАЧАЛА получаем reviewId ДО удаления комментария
            Long reviewId = commentService.getReviewIdByCommentId(id);
            Long bookId = commentService.getBookIdByReviewId(reviewId);

            // ПОТОМ удаляем комментарий
            commentService.deleteComment(id, username);

            return "redirect:/books/" + bookId + "#review-" + reviewId;
        } catch (RuntimeException e) {
            // Логируем ошибку
            System.err.println("Error deleting comment: " + e.getMessage());
            // Перенаправляем на главную страницу книг в случае ошибки
            return "redirect:/books?error=comment_not_found";
        }
    }
}