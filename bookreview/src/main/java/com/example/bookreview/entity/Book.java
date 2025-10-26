package com.example.bookreview.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Entity
@Data
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String author;
    @Column(length = 2000)
    private String description;
    private String coverUrl;
    private String fileUrl;
    private Boolean isActive;
}
