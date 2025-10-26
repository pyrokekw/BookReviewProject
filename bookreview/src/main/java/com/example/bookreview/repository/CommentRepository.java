package com.example.bookreview.repository;

import com.example.bookreview.entity.Comment;
import com.example.bookreview.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.user WHERE c.id = :commentId")
    Optional<Comment> findByIdWithUser(@Param("commentId") Long commentId);

    @Query("SELECT c.review.id FROM Comment c WHERE c.id = :commentId")
    Optional<Long> findReviewIdByCommentId(@Param("commentId") Long commentId);
}