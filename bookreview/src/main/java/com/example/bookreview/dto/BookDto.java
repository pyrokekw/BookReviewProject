package com.example.bookreview.dto;

import lombok.Data;

@Data
public class BookDto {
    private Long id;
    private String title;
    private String author;
    private String description;
    private String coverUrl;
    private String fileUrl;
    private Boolean isActive;
}
