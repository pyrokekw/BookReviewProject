package com.example.bookreview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentCreateDto {
    private Long reviewId;
    private String text;

    // геттеры и сеттеры
    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}