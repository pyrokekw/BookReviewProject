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
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;

    @PostMapping("/reviews/add")
    public String addReview(@RequestParam Long bookId,
                            @RequestParam String text,
                            Principal principal) {

        reviewService.addReview(bookId, principal.getName(), text);
        return "redirect:/books/" + bookId + "#reviews-section";
    }

    @PostMapping("/reviews/{id}/delete")
    public String deleteReview(@PathVariable("id") Long id, Principal principal) {
        String username = principal.getName();
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));

        Long bookId = review.getBook().getId();
        reviewService.deleteReview(id, username);

        return "redirect:/books/" + bookId;
    }

    @PostMapping("/reviews/update")
    public String updateReview(@RequestParam Long reviewId,
                               @RequestParam String text,
                               Principal principal) {

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