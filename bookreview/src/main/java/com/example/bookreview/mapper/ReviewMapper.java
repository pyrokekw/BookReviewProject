package com.example.bookreview.mapper;

import com.example.bookreview.dto.ReviewDto;
import com.example.bookreview.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "likesCount", expression = "java(review.getLikes() != null ? review.getLikes().size() : 0)")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "liked", ignore = true)
    @Mapping(target = "comments", ignore = true)
    ReviewDto toDto(Review review);

    List<ReviewDto> toDtoList(List<Review> reviews);
}