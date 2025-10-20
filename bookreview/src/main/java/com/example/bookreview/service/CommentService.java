package com.example.bookreview.service;

import com.example.bookreview.dto.CommentCreateDto;
import com.example.bookreview.dto.CommentDto;
import com.example.bookreview.entity.Comment;
import com.example.bookreview.entity.Review;
import com.example.bookreview.entity.User;
import com.example.bookreview.exception.BusinessException;
import com.example.bookreview.exception.ResourceNotFoundException;
import com.example.bookreview.mapper.CommentMapper;
import com.example.bookreview.repository.CommentRepository;
import com.example.bookreview.repository.ReviewRepository;
import com.example.bookreview.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Transactional
    public CommentDto addComment(CommentCreateDto commentCreateDto, String username) {
        Review review = reviewRepository.findById(commentCreateDto.getReviewId())
                .orElseThrow(() -> new ResourceNotFoundException("Рецензия", commentCreateDto.getReviewId()));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", username));

        Comment comment = new Comment();
        comment.setText(commentCreateDto.getText());
        comment.setReview(review);
        comment.setUser(user);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        commentRepository.save(comment);
        return commentMapper.toDto(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findByIdWithUser(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Комментарий", commentId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", username));

        boolean isAuthor = comment.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole().equals("ROLE_ADMIN");

        if (!isAuthor && !isAdmin) {
            throw new BusinessException("У вас нет прав для удаления этого комментария");
        }

        commentRepository.delete(comment);
    }

    public Long getReviewIdByCommentId(Long commentId) {
        return commentRepository.findReviewIdByCommentId(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Комментарий", commentId));
    }

    public Long getBookIdByReviewId(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Рецензия", reviewId));
        return review.getBook().getId();
    }
}