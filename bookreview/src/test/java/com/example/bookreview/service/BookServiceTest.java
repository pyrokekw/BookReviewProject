package com.example.bookreview.service;

import com.example.bookreview.dto.BookCreateDto;
import com.example.bookreview.dto.BookDto;
import com.example.bookreview.entity.Book;
import com.example.bookreview.exception.BusinessException;
import com.example.bookreview.exception.ResourceNotFoundException;
import com.example.bookreview.mapper.BookMapper;
import com.example.bookreview.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookService bookService;

    private Book createSampleBook() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setDescription("Test Description");
        book.setCoverUrl("http://example.com/cover.jpg");
        book.setIsActive(true);
        return book;
    }

    private BookDto createSampleBookDto() {
        return BookDto.builder()
                .id(1L)
                .title("Test Book")
                .author("Test Author")
                .description("Test Description")
                .coverUrl("http://example.com/cover.jpg")
                .isActive(true)
                .build();
    }

    private BookCreateDto createSampleBookCreateDto() {
        BookCreateDto dto = new BookCreateDto();
        dto.setTitle("New Book");
        dto.setAuthor("New Author");
        dto.setDescription("New Description");
        dto.setCoverUrl("http://example.com/cover.jpg");
        return dto;
    }

    @Test
    void createBook_WithValidData_ShouldSaveAndReturnBook() {

        BookCreateDto dto = createSampleBookCreateDto();
        Book book = createSampleBook();
        BookDto expectedDto = createSampleBookDto();

        when(bookRepository.existsByTitleIgnoreCase(dto.getTitle())).thenReturn(false);
        when(bookMapper.toEntity(dto)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(expectedDto);

        BookDto result = bookService.createBook(dto);

        assertNotNull(result);
        assertEquals(expectedDto.getId(), result.getId());
        assertTrue(book.getIsActive());
        verify(bookRepository).existsByTitleIgnoreCase(dto.getTitle());
        verify(bookRepository).save(book);
    }

    @Test
    void createBook_WithDuplicateTitle_ShouldThrowBusinessException() {

        BookCreateDto dto = createSampleBookCreateDto();
        when(bookRepository.existsByTitleIgnoreCase(dto.getTitle())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> bookService.createBook(dto));

        assertTrue(exception.getMessage().contains("уже существует"));
        verify(bookRepository, never()).save(any());
    }

    @Test
    void updateBook_WithValidData_ShouldUpdateAndReturnBook() {

        Long bookId = 1L;
        BookCreateDto dto = createSampleBookCreateDto();
        Book existingBook = createSampleBook();
        BookDto expectedDto = createSampleBookDto();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.findByTitleIgnoreCaseAndIdNot(dto.getTitle(), bookId))
                .thenReturn(Optional.empty());
        when(bookRepository.save(existingBook)).thenReturn(existingBook);
        when(bookMapper.toDto(existingBook)).thenReturn(expectedDto);

        BookDto result = bookService.updateBook(bookId, dto);

        assertNotNull(result);
        assertEquals(dto.getTitle(), existingBook.getTitle());
        assertEquals(dto.getAuthor(), existingBook.getAuthor());
        verify(bookRepository).save(existingBook);
    }

    @Test
    void getBookById_WithNonExistingBook_ShouldThrowResourceNotFoundException() {

        Long bookId = 999L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookService.getBookById(bookId));

        verify(bookRepository).findById(bookId);
        verify(bookMapper, never()).toDto(any());
    }

    @Test
    void deactivateBook_WithExistingBook_ShouldSetIsActiveToFalse() {

        Long bookId = 1L;
        Book book = createSampleBook();
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        bookService.deactivateBook(bookId);

        assertFalse(book.getIsActive());
        verify(bookRepository).save(book);
    }

    @Test
    void searchBooks_WithValidQuery_ShouldReturnFilteredPage() {

        String query = "test";
        Pageable pageable = PageRequest.of(0, 9);
        Page<Book> bookPage = new PageImpl<>(Arrays.asList(createSampleBook()));
        BookDto bookDto = createSampleBookDto();

        when(bookRepository.findByTitleOrAuthorContainingIgnoreCase(eq(query), any(Pageable.class)))
                .thenReturn(bookPage);
        when(bookMapper.toDto(any(Book.class))).thenReturn(bookDto);

        Page<BookDto> result = bookService.searchBooks(query, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(bookRepository).findByTitleOrAuthorContainingIgnoreCase(query, pageable);
    }

    @Test
    void filterByAuthor_WithAllKeyword_ShouldReturnAllBooks() {

        String author = "all";
        Pageable pageable = PageRequest.of(0, 9);
        Page<Book> bookPage = new PageImpl<>(Arrays.asList(createSampleBook()));
        BookDto bookDto = createSampleBookDto();

        when(bookRepository.findByIsActiveTrue(pageable)).thenReturn(bookPage);
        when(bookMapper.toDto(any(Book.class))).thenReturn(bookDto);

        Page<BookDto> result = bookService.filterByAuthor(author, pageable);

        assertNotNull(result);
        verify(bookRepository).findByIsActiveTrue(pageable);
        verify(bookRepository, never()).findByAuthorContainingIgnoreCase(anyString(), any());
    }

    @Test
    void getBooksPerPage_ShouldReturnConfiguredValue() {

        int result = bookService.getBooksPerPage();

        assertEquals(9, result);
    }

    @Test
    void getAllAuthors_ShouldReturnDistinctAuthorsList() {

        List<String> authors = Arrays.asList("Author 1", "Author 2");
        when(bookRepository.findAllActiveAuthors()).thenReturn(authors);

        List<String> result = bookService.getAllAuthors();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookRepository).findAllActiveAuthors();
    }

    @Test
    void searchBooks_WithEmptyQuery_ShouldReturnAllBooks() {

        String query = "";
        Pageable pageable = PageRequest.of(0, 9);
        Page<Book> bookPage = new PageImpl<>(Arrays.asList(createSampleBook()));
        BookDto bookDto = createSampleBookDto();

        when(bookRepository.findByIsActiveTrue(pageable)).thenReturn(bookPage);
        when(bookMapper.toDto(any(Book.class))).thenReturn(bookDto);

        Page<BookDto> result = bookService.searchBooks(query, pageable);

        assertNotNull(result);
        verify(bookRepository).findByIsActiveTrue(pageable);
        verify(bookRepository, never()).findByTitleOrAuthorContainingIgnoreCase(anyString(), any());
    }
}