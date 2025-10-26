package com.example.bookreview.controller;

import com.example.bookreview.dto.BookCreateDto;
import com.example.bookreview.dto.BookDto;
import com.example.bookreview.dto.ReviewDto;
import com.example.bookreview.service.BookService;
import com.example.bookreview.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private ReviewService reviewService;

    private BookDto createSampleBookDto() {
        return BookDto.builder()
                .id(1L)
                .title("Test Book")
                .author("Test Author")
                .description("Test Description")
                .coverUrl("http://example.com/cover.jpg")
                .fileUrl(null)
                .isActive(true)
                .build();
    }

    private ReviewDto createSampleReviewDto() {
        return ReviewDto.builder()
                .id(1L)
                .text("Great book!")
                .username("testuser")
                .likesCount(5L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .liked(false)
                .comments(null)
                .build();
    }

    @Test
    @WithMockUser
    void showAddBookForm_ShouldReturnAddBookView() throws Exception {
        mockMvc.perform(get("/books/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/add"))
                .andExpect(model().attributeExists("bookCreateDto"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addBook_WithValidData_ShouldRedirectToList() throws Exception {
        BookCreateDto validDto = new BookCreateDto();
        validDto.setTitle("New Book");
        validDto.setAuthor("New Author");
        validDto.setDescription("New Description");

        mockMvc.perform(post("/books/add")
                        .with(csrf())
                        .param("title", validDto.getTitle())
                        .param("author", validDto.getAuthor())
                        .param("description", validDto.getDescription()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books?success"));

        verify(bookService).createBook(any(BookCreateDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addBook_WithInvalidData_ShouldReturnToForm() throws Exception {
        mockMvc.perform(post("/books/add")
                        .with(csrf())
                        .param("title", "")
                        .param("author", "Author"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/add"))
                .andExpect(model().attributeHasFieldErrors("bookCreateDto", "title"));

        verify(bookService, never()).createBook(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showEditBookForm_WithExistingBook_ShouldReturnEditView() throws Exception {
        BookDto bookDto = createSampleBookDto();
        when(bookService.getBookForEdit(1L)).thenReturn(bookDto);

        mockMvc.perform(get("/books/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/edit"))
                .andExpect(model().attributeExists("bookCreateDto"))
                .andExpect(model().attributeExists("bookId"));

        verify(bookService).getBookForEdit(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateBook_WithValidData_ShouldRedirectToBookView() throws Exception {
        mockMvc.perform(post("/books/1/edit")
                        .with(csrf())
                        .param("title", "Updated Book")
                        .param("author", "Updated Author")
                        .param("description", "Updated Description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1?success"));

        verify(bookService).updateBook(eq(1L), any(BookCreateDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateBook_ShouldRedirectToList() throws Exception {
        mockMvc.perform(post("/books/1/deactivate")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).deactivateBook(1L);
    }

    @Test
    @WithMockUser
    void viewBook_WithAuthenticatedUser_ShouldReturnBookView() throws Exception {
        BookDto bookDto = createSampleBookDto();
        List<ReviewDto> reviews = Arrays.asList(createSampleReviewDto());

        when(bookService.getBookById(1L)).thenReturn(bookDto);
        when(reviewService.getReviewsForBook(eq(1L), anyString())).thenReturn(reviews);

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/view"))
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attributeExists("reviews"))
                .andExpect(model().attributeExists("reviewCreateDto"));

        verify(bookService).getBookById(1L);
        verify(reviewService).getReviewsForBook(1L, "user");
    }

    @Test
    @WithMockUser
    void viewBook_WithoutAuthentication_ShouldReturnBookView() throws Exception {
        BookDto bookDto = createSampleBookDto();
        List<ReviewDto> reviews = Arrays.asList(createSampleReviewDto());

        when(bookService.getBookById(1L)).thenReturn(bookDto);
        when(reviewService.getReviewsForBook(eq(1L), anyString())).thenReturn(reviews);

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/view"))
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attributeExists("reviews"));

        verify(reviewService).getReviewsForBook(1L, "user");
    }

    @Test
    @WithMockUser
    void listBooks_WithPagination_ShouldReturnListView() throws Exception {
        Page<BookDto> bookPage = new PageImpl<>(Arrays.asList(createSampleBookDto()));
        List<String> authors = Arrays.asList("Author 1", "Author 2");

        when(bookService.getAllBooks(any(Pageable.class))).thenReturn(bookPage);
        when(bookService.getAllAuthors()).thenReturn(authors);
        when(bookService.getBooksPerPage()).thenReturn(9);

        mockMvc.perform(get("/books?page=0"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attributeExists("books"))
                .andExpect(model().attributeExists("authors"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("totalPages"));

        verify(bookService).getAllBooks(any(Pageable.class));
    }

    @Test
    @WithMockUser
    void listBooks_WithSearchQuery_ShouldReturnFilteredResults() throws Exception {
        Page<BookDto> bookPage = new PageImpl<>(Arrays.asList(createSampleBookDto()));
        List<String> authors = Arrays.asList("Author 1");

        when(bookService.searchBooks(eq("test"), argThat(pageable -> pageable.getPageSize() == 9)))
                .thenReturn(bookPage);
        when(bookService.getAllAuthors()).thenReturn(authors);
        when(bookService.getBooksPerPage()).thenReturn(9);

        mockMvc.perform(get("/books?search=test"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attributeExists("currentSearch"))
                .andExpect(model().attribute("currentSearch", "test"));

        verify(bookService).searchBooks(eq("test"), any(Pageable.class));
    }

    @Test
    @WithMockUser
    void listBooks_WithAuthorFilter_ShouldReturnFilteredResults() throws Exception {
        Page<BookDto> bookPage = new PageImpl<>(Arrays.asList(createSampleBookDto()));
        List<String> authors = Arrays.asList("Test Author");

        when(bookService.filterByAuthor(eq("Test Author"), argThat(pageable -> pageable.getPageSize() == 9)))
                .thenReturn(bookPage);
        when(bookService.getAllAuthors()).thenReturn(authors);
        when(bookService.getBooksPerPage()).thenReturn(9);

        mockMvc.perform(get("/books?author=Test Author"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attributeExists("currentAuthor"))
                .andExpect(model().attribute("currentAuthor", "Test Author"));

        verify(bookService).filterByAuthor(eq("Test Author"), any(Pageable.class));
    }

    @Test
    @WithMockUser
    void listBooks_WithSuccessParam_ShouldAddSuccessMessage() throws Exception {
        Page<BookDto> bookPage = new PageImpl<>(Arrays.asList(createSampleBookDto()));
        when(bookService.getAllBooks(any(Pageable.class))).thenReturn(bookPage);
        when(bookService.getAllAuthors()).thenReturn(Arrays.asList("Author"));
        when(bookService.getBooksPerPage()).thenReturn(9);

        mockMvc.perform(get("/books?success=true"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("successMessage"));
    }
}