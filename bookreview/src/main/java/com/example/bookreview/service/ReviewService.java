package com.example.bookreview.service;

import com.example.bookreview.dto.CommentDto;
import com.example.bookreview.dto.ReviewDto;
import com.example.bookreview.entity.Book;
import com.example.bookreview.entity.Comment;
import com.example.bookreview.entity.Review;
import com.example.bookreview.entity.User;
import com.example.bookreview.exception.BusinessException;
import com.example.bookreview.exception.ResourceNotFoundException;
import com.example.bookreview.mapper.CommentMapper;
import com.example.bookreview.mapper.ReviewMapper;
import com.example.bookreview.repository.BookRepository;
import com.example.bookreview.repository.ReviewRepository;
import com.example.bookreview.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;
    private final CommentMapper commentMapper;

    @Transactional
    public ReviewDto addReview(Long bookId, String username, String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new BusinessException("Текст рецензии не может быть пустым");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Книга", bookId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", username)); // Теперь работает

        // Проверяем, не оставлял ли пользователь уже рецензию на эту книгу
        if (reviewRepository.findByBookAndUser(book, user).isPresent()) {
            throw new BusinessException("Вы уже оставляли рецензию на эту книгу");
        }

        Review review = new Review();
        review.setBook(book);
        review.setUser(user);
        review.setText(text);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        reviewRepository.save(review);
        return reviewMapper.toDto(review);
    }

    public List<ReviewDto> getReviewsForBook(long bookId, String currentUsername) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Книга", bookId));

        List<Review> reviews = reviewRepository.findByBookWithUserAndLikes(book);
        List<Review> reviewsWithComments = reviewRepository.findByBookWithComments(book);

        // Отладочная информация (можно убрать в продакшене)
        System.out.println("=== DEBUG COMMENTS ===");
        for (Review review : reviewsWithComments) {
            if (review.getComments() != null) {
                System.out.println("Review ID: " + review.getId() +
                        ", Comments count: " + review.getComments().size());
            }
        }

        Map<Long, List<CommentDto>> commentsByReviewId = extractCommentsMap(reviewsWithComments);
        List<ReviewDto> reviewDtos = reviewMapper.toDtoList(reviews);

        if (currentUsername != null) {
            setLikedStatusForUser(reviewDtos, reviews, currentUsername);
        }

        setCommentsForReviews(reviewDtos, commentsByReviewId);
        return reviewDtos;
    }

    private Map<Long, List<CommentDto>> extractCommentsMap(List<Review> reviewsWithComments) {
        return reviewsWithComments.stream()
                .collect(Collectors.toMap(
                        Review::getId,
                        review -> {
                            if (review.getComments() == null) {
                                return new ArrayList<>();
                            }
                            List<Comment> nonNullComments = review.getComments().stream()
                                    .filter(comment -> comment != null)
                                    .collect(Collectors.toList());
                            return commentMapper.toDtoList(nonNullComments);
                        }
                ));
    }

    private void setLikedStatusForUser(List<ReviewDto> reviewDtos, List<Review> reviews, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", username));

        for (int i = 0; i < reviews.size(); i++) {
            Review review = reviews.get(i);
            ReviewDto reviewDto = reviewDtos.get(i);

            boolean isLiked = review.getLikes().stream()
                    .anyMatch(like -> like.getUser().getId().equals(user.getId()));
            reviewDto.setLiked(isLiked);
        }
    }

    private void setCommentsForReviews(List<ReviewDto> reviewDtos, Map<Long, List<CommentDto>> commentsByReviewId) {
        for (ReviewDto reviewDto : reviewDtos) {
            List<CommentDto> comments = commentsByReviewId.get(reviewDto.getId());
            if (comments != null) {
                reviewDto.setComments(comments);
            }
        }
    }

    @Transactional
    public void deleteReview(Long reviewId, String username) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Рецензия", reviewId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", username));

        boolean isAuthor = review.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole().equals("ROLE_ADMIN");

        if (!isAuthor && !isAdmin) {
            throw new BusinessException("У вас нет прав для удаления этой рецензии");
        }

        reviewRepository.delete(review);
    }

    public Long getBookIdByReviewId(Long reviewId) {
        return reviewRepository.findBookIdByReviewId(reviewId);
    }
}