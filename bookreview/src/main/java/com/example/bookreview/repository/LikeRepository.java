package com.example.bookreview.repository;

import com.example.bookreview.entity.Like;
import com.example.bookreview.entity.Review;
import com.example.bookreview.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserAndReview(User user, Review review);
    long countByReview(Review review);
}
