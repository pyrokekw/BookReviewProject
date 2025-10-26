package com.example.bookreview.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookDto {
    private Long id;
    private String title;
    private String author;
    private String description;
    private String coverUrl;
    private String fileUrl;
    private Boolean isActive;
}
