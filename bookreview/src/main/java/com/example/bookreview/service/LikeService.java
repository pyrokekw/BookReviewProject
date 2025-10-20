package com.example.bookreview.service;

import com.example.bookreview.entity.Like;
import com.example.bookreview.entity.Review;
import com.example.bookreview.entity.User;
import com.example.bookreview.exception.BusinessException;
import com.example.bookreview.exception.ResourceNotFoundException;
import com.example.bookreview.repository.LikeRepository;
import com.example.bookreview.repository.ReviewRepository;
import com.example.bookreview.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public long likeReview(Long reviewId, String username) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Рецензия", reviewId));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", username));

        if (review.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Нельзя оценивать собственную рецензию");
        }

        Optional<Like> existingLike = likeRepository.findByUserAndReview(user, review);

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
        } else {
            Like like = new Like();
            like.setUser(user);
            like.setReview(review);
            likeRepository.save(like);
        }

        return likeRepository.countByReview(review);
    }
}