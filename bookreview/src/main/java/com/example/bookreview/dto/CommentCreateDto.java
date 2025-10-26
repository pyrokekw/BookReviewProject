package com.example.bookreview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter @Setter
public class CommentCreateDto {
    private Long reviewId;
    @NotBlank
    private String text;
}