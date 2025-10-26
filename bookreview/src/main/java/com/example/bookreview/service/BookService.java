package com.example.bookreview.service;

import com.example.bookreview.dto.BookCreateDto;
import com.example.bookreview.dto.BookDto;
import com.example.bookreview.entity.Book;
import com.example.bookreview.exception.BusinessException;
import com.example.bookreview.exception.ResourceNotFoundException;
import com.example.bookreview.mapper.BookMapper;
import com.example.bookreview.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    private static final String DEFAULT_COVER_URL = "https://i.pinimg.com/736x/69/e8/c8/69e8c85300a6d61b2b188930b4f2881b.jpg";
    private static final int BOOKS_PER_PAGE = 9;

    public Page<BookDto> getAllBooks(Pageable pageable) {
        Page<Book> bookPage = bookRepository.findByIsActiveTrue(pageable);
        List<BookDto> bookDtos = bookPage.getContent().stream()
                .map(bookMapper::toDto)
                .peek(this::setDefaultCoverIfNeeded)
                .collect(Collectors.toList());
        return new PageImpl<>(bookDtos, pageable, bookPage.getTotalElements());
    }

    public Page<BookDto> searchBooks(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return getAllBooks(pageable);
        }
        Page<Book> bookPage = bookRepository.findByTitleOrAuthorContainingIgnoreCase(query.trim(), pageable);
        List<BookDto> bookDtos = bookPage.getContent().stream()
                .map(bookMapper::toDto)
                .peek(this::setDefaultCoverIfNeeded)
                .collect(Collectors.toList());
        return new PageImpl<>(bookDtos, pageable, bookPage.getTotalElements());
    }

    public Page<BookDto> filterByAuthor(String author, Pageable pageable) {
        if (author == null || author.trim().isEmpty() || "all".equals(author)) {
            return getAllBooks(pageable);
        }
        Page<Book> bookPage = bookRepository.findByAuthorContainingIgnoreCase(author.trim(), pageable);
        List<BookDto> bookDtos = bookPage.getContent().stream()
                .map(bookMapper::toDto)
                .peek(this::setDefaultCoverIfNeeded)
                .collect(Collectors.toList());
        return new PageImpl<>(bookDtos, pageable, bookPage.getTotalElements());
    }

    public BookDto getBookForEdit(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Книга", id));
        return bookMapper.toDto(book);
    }

    public BookDto updateBook(Long id, BookCreateDto dto) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Книга", id));

        if (bookRepository.findByTitleIgnoreCaseAndIdNot(dto.getTitle(), id).isPresent()) {
            throw new BusinessException("Книга с названием '" + dto.getTitle() + "' уже существует");
        }

        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setDescription(dto.getDescription());
        book.setCoverUrl(getValidCoverUrl(dto.getCoverUrl()));
        book.setFileUrl(dto.getFileUrl());

        bookRepository.save(book);
        return bookMapper.toDto(book);
    }

    public BookDto createBook(BookCreateDto dto) {
        if (bookRepository.existsByTitleIgnoreCase(dto.getTitle())) {
            throw new BusinessException("Книга с названием '" + dto.getTitle() + "' уже существует");
        }

        Book book = bookMapper.toEntity(dto);
        book.setIsActive(true);
        book.setCoverUrl(getValidCoverUrl(dto.getCoverUrl()));
        bookRepository.save(book);

        BookDto result = bookMapper.toDto(book);
        setDefaultCoverIfNeeded(result);
        return result;
    }

    public void deactivateBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Книга", id));
        book.setIsActive(false);
        bookRepository.save(book);
    }

    public BookDto getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Книга", id));
        BookDto dto = bookMapper.toDto(book);

        setDefaultCoverIfNeeded(dto);

        if (dto.getFileUrl() == null || dto.getFileUrl().trim().isEmpty() ||
                dto.getFileUrl().equals("/books/") || !isValidUrl(dto.getFileUrl())) {
            dto.setFileUrl(null);
        }

        return dto;
    }

    public List<String> getAllAuthors() {
        return bookRepository.findAllActiveAuthors();
    }

    public int getBooksPerPage() {
        return BOOKS_PER_PAGE;
    }

    private void setDefaultCoverIfNeeded(BookDto book) {
        if (book.getCoverUrl() == null || book.getCoverUrl().trim().isEmpty()) {
            book.setCoverUrl(DEFAULT_COVER_URL);
        }
    }

    private String getValidCoverUrl(String coverUrl) {
        if (coverUrl == null || coverUrl.trim().isEmpty()) {
            return DEFAULT_COVER_URL;
        }
        return coverUrl;
    }

    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url);
            return url.startsWith("http://") || url.startsWith("https://");
        } catch (Exception e) {
            return false;
        }
    }
}