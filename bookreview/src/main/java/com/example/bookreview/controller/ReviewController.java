package com.example.bookreview.controller;

import com.example.bookreview.entity.Review;
import com.example.bookreview.repository.ReviewRepository;
import com.example.bookreview.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ReviewController { // УБИРАЕМ @RequestMapping("/reviews")

    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;

    // ДОБАВЛЕНИЕ РЕЦЕНЗИИ
    @PostMapping("/reviews/add")
    public String addReview(@RequestParam Long bookId,
                            @RequestParam String text,
                            Principal principal) {
        System.out.println("=== ADD REVIEW ===");
        System.out.println("Book ID: " + bookId);
        System.out.println("Text: " + text);
        System.out.println("User: " + principal.getName());

        // ИСПРАВЛЕНИЕ: правильный порядок параметров
        reviewService.addReview(bookId, principal.getName(), text);
        return "redirect:/books/" + bookId + "#reviews-section";
    }

    // УДАЛЕНИЕ РЕЦЕНЗИИ
    @PostMapping("/reviews/{id}/delete")
    public String deleteReview(@PathVariable("id") Long id, Principal principal) {
        System.out.println("=== DELETE REVIEW ===");
        System.out.println("Review ID: " + id);
        System.out.println("User: " + principal.getName());

        String username = principal.getName();
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));

        Long bookId = review.getBook().getId();
        reviewService.deleteReview(id, username);

        return "redirect:/books/" + bookId;
    }

    // ОБНОВЛЕНИЕ РЕЦЕНЗИИ
    @PostMapping("/reviews/update")
    public String updateReview(@RequestParam Long reviewId,
                               @RequestParam String text,
                               Principal principal) {
        System.out.println("=== UPDATE REVIEW ===");
        System.out.println("Review ID: " + reviewId);
        System.out.println("User: " + principal.getName());

        String username = principal.getName();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Not allowed to edit this review");
        }

        review.setText(text);
        review.setUpdatedAt(LocalDateTime.now());
        reviewRepository.save(review);

        return "redirect:/books/" + review.getBook().getId();
    }
}