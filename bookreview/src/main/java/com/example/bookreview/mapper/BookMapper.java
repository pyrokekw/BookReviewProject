package com.example.bookreview.mapper;

import com.example.bookreview.dto.BookCreateDto;
import com.example.bookreview.dto.BookDto;
import com.example.bookreview.entity.Book;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookMapper {
    BookDto toDto(Book book);
    Book toEntity(BookCreateDto dto);
    List<BookDto> toDtoList(List<Book> books);
}
