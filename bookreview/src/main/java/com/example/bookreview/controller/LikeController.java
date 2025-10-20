package com.example.bookreview.controller;

import com.example.bookreview.entity.Review;
import com.example.bookreview.repository.ReviewRepository;
import com.example.bookreview.service.LikeService;
import com.example.bookreview.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class LikeController { // УБЕРИТЕ @RequestMapping("/likes")

    private final LikeService likeService;
    private final ReviewRepository reviewRepository;

    @PostMapping("/likes/review/{id}")
    public String likeReview(@PathVariable Long id,
                             Principal principal,
                             HttpServletRequest request) {
        try {
            likeService.likeReview(id, principal.getName());

            // Получаем bookId из репозитория
            Review review = reviewRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Review not found"));

            Long bookId = review.getBook().getId();
            return "redirect:/books/" + bookId + "#review-" + id;

        } catch (RuntimeException e) {
            System.out.println("Like error: " + e.getMessage());

            // Если нельзя лайкать свою рецензию - просто возвращаем на ту же страницу
            if (e.getMessage().contains("Cannot like your own review")) {
                // Получаем bookId даже при ошибке
                Review review = reviewRepository.findById(id).orElse(null);
                if (review != null && review.getBook() != null) {
                    return "redirect:/books/" + review.getBook().getId() + "#review-" + id;
                }
            }

            // Возвращаем на предыдущую страницу при любой другой ошибке
            String referer = request.getHeader("Referer");
            return "redirect:" + (referer != null ? referer : "/books");
        }
    }
}