package com.example.bookreview.dto;

import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewDto {
    private Long id;
    private String text;
    private String username;
    private long likesCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Setter
    private boolean liked; // Добавляем сеттер!
    private List<CommentDto> comments;
}
