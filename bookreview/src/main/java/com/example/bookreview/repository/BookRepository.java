package com.example.bookreview.repository;

import com.example.bookreview.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Page<Book> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.isActive = true AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Book> findByTitleOrAuthorContainingIgnoreCase(@Param("query") String query, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.isActive = true AND LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))")
    Page<Book> findByAuthorContainingIgnoreCase(@Param("author") String author, Pageable pageable);

    @Query("SELECT DISTINCT b.author FROM Book b WHERE b.isActive = true AND b.author IS NOT NULL ORDER BY b.author")
    List<String> findAllActiveAuthors();

    @Query("SELECT COUNT(b) > 0 FROM Book b WHERE LOWER(b.title) = LOWER(:title) AND b.isActive = true")
    boolean existsByTitleIgnoreCase(@Param("title") String title);

    @Query("SELECT b FROM Book b WHERE b.isActive = true AND LOWER(b.title) = LOWER(:title) AND b.id != :id")
    Optional<Book> findByTitleIgnoreCaseAndIdNot(@Param("title") String title, @Param("id") Long id);
}