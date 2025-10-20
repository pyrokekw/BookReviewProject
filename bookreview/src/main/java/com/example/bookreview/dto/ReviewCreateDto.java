package com.example.bookreview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewCreateDto {
    @NotBlank(message = "Текст рецензии не может быть пустым")
    private String text;
    private Long bookId;
}