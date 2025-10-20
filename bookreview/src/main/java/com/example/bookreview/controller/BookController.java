package com.example.bookreview.controller;

import com.example.bookreview.dto.BookCreateDto;
import com.example.bookreview.dto.BookDto;
import com.example.bookreview.dto.ReviewCreateDto;
import com.example.bookreview.dto.ReviewDto;
import com.example.bookreview.service.BookService;
import com.example.bookreview.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final ReviewService reviewService;

    @GetMapping("/add")
    public String addBookForm(Model model) {
        model.addAttribute("bookCreateDto", new BookCreateDto());
        return "books/add";
    }

    @PostMapping("/add")
    public String addBook(@Valid @ModelAttribute("bookCreateDto") BookCreateDto dto,
                          BindingResult result) {
        if (result.hasErrors()) {
            return "books/add";
        }

        bookService.createBook(dto);
        return "redirect:/books?success";
    }

    @GetMapping("/{id}/edit")
    public String editBookForm(@PathVariable Long id, Model model) {
        BookDto book = bookService.getBookForEdit(id);
        BookCreateDto bookCreateDto = new BookCreateDto();

        bookCreateDto.setTitle(book.getTitle());
        bookCreateDto.setAuthor(book.getAuthor());
        bookCreateDto.setDescription(book.getDescription());
        bookCreateDto.setCoverUrl(book.getCoverUrl());
        bookCreateDto.setFileUrl(book.getFileUrl());

        model.addAttribute("bookCreateDto", bookCreateDto);
        model.addAttribute("bookId", id);
        return "books/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateBook(@PathVariable Long id,
                             @Valid @ModelAttribute("bookCreateDto") BookCreateDto dto,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("bookId", id);
            return "books/edit";
        }

        bookService.updateBook(id, dto);
        return "redirect:/books/" + id + "?success";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivateBook(@PathVariable Long id) {
        bookService.deactivateBook(id);
        return "redirect:/books";
    }

    @GetMapping("/{id}")
    public String viewBook(@PathVariable Long id,
                           @RequestParam(required = false) String success,
                           Model model, Principal principal) {
        BookDto book = bookService.getBookById(id);

        String username = principal != null ? principal.getName() : null;
        List<ReviewDto> reviews = reviewService.getReviewsForBook(id, username);

        model.addAttribute("book", book);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewCreateDto", new ReviewCreateDto());

        if (success != null) {
            model.addAttribute("successMessage", "Информация о книге успешно обновлена!");
        }

        return "books/view";
    }

    @GetMapping
    public String listBooks(@RequestParam(required = false) String search,
                            @RequestParam(required = false) String author,
                            @RequestParam(required = false) String success,
                            @RequestParam(defaultValue = "0") int page,
                            Model model, Principal principal) {

        Pageable pageable = PageRequest.of(page, bookService.getBooksPerPage(), Sort.by("title").ascending());
        Page<BookDto> books;

        if (search != null && !search.trim().isEmpty()) {
            books = bookService.searchBooks(search, pageable);
            model.addAttribute("currentSearch", search.trim());
        } else if (author != null && !author.equals("all")) {
            books = bookService.filterByAuthor(author, pageable);
            model.addAttribute("currentAuthor", author);
        } else {
            books = bookService.getAllBooks(pageable);
        }

        List<String> authors = bookService.getAllAuthors();

        model.addAttribute("books", books);
        model.addAttribute("authors", authors);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", books.getTotalPages());
        model.addAttribute("totalItems", books.getTotalElements());

        if (success != null) {
            model.addAttribute("successMessage", "Книга успешно добавлена!");
        }

        if (principal != null) {
            System.out.println("Current user: " + principal.getName());
        }

        return "books/list";
    }
}