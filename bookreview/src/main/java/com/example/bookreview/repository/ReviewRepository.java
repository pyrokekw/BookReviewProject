package com.example.bookreview.repository;

import com.example.bookreview.entity.Book;
import com.example.bookreview.entity.Review;
import com.example.bookreview.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // загружаем только пользователя и лайки
    @Query("SELECT DISTINCT r FROM Review r " +
            "LEFT JOIN FETCH r.user " +
            "LEFT JOIN FETCH r.likes " +
            "WHERE r.book = :book " +
            "ORDER BY r.createdAt DESC")
    List<Review> findByBookWithUserAndLikes(@Param("book") Book book);

    // загрузка комментариев (без одновременной загрузки лайков)
    @Query("SELECT DISTINCT r FROM Review r " +
            "LEFT JOIN FETCH r.comments c " +
            "LEFT JOIN FETCH c.user " +
            "WHERE r.book = :book " +
            "ORDER BY r.createdAt DESC, c.createdAt ASC")
    List<Review> findByBookWithComments(@Param("book") Book book);

    Optional<Review> findByBookAndUser(Book book, User user);
}