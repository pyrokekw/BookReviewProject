package com.example.bookreview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookCreateDto {
    @NotBlank
    private String title;
    @NotBlank
    private String author;
    private String description;
    private String coverUrl;
    private String fileUrl;
}

