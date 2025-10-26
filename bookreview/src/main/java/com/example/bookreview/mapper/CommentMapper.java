package com.example.bookreview.mapper;

import com.example.bookreview.dto.CommentDto;
import com.example.bookreview.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "userUsername", source = "user.username", qualifiedByName = "getSafeUsername")
    @Mapping(target = "reviewId", source = "review.id")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "text", source = "text", qualifiedByName = "getSafeText")
    CommentDto toDto(Comment comment);

    default List<CommentDto> toDtoList(List<Comment> comments) {
        if (comments == null) {
            return new ArrayList<>();
        }

        return comments.stream()
                .filter(comment -> comment != null)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Named("getSafeUsername")
    default String getSafeUsername(String username) {
        return username != null ? username : "Аноним";
    }

    @Named("getSafeText")
    default String getSafeText(String text) {
        return text != null ? text : "";
    }
}