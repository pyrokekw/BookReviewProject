package com.example.bookreview.exception;

import com.example.bookreview.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // Обработка кастомных бизнес-исключений
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.error("Business exception: {}", ex.getMessage());

        ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorResponse",
                new ErrorResponse(ex.getMessage(), "Business Error", 400, request.getRequestURI()));
        return mav;
    }

    // Обработка ошибок валидации
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationExceptions(MethodArgumentNotValidException ex, Model model) {
        BindingResult result = ex.getBindingResult();
        String errorMessage = result.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.error("Validation error: {}", errorMessage);

        // Возвращаемся на ту же форму с ошибками
        String viewName = determineViewName(ex);
        model.addAttribute("org.springframework.validation.BindingResult." +
                result.getObjectName(), result);

        // Восстанавливаем объект DTO
        try {
            model.addAttribute(result.getObjectName(), ex.getBindingResult().getTarget());
        } catch (Exception e) {
            // Игнорируем если не можем восстановить объект
        }

        return viewName;
    }

    // Обработка RuntimeException (общие ошибки)
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        log.error("Runtime exception: ", ex);

        ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorResponse",
                new ErrorResponse("Произошла внутренняя ошибка сервера", "Internal Server Error", 500, request.getRequestURI()));
        return mav;
    }

    // Обработка 404 ошибок
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        log.error("Resource not found: {}", ex.getMessage());

        ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorResponse",
                new ErrorResponse(ex.getMessage(), "Not Found", 404, request.getRequestURI()));
        return mav;
    }

    // Обработка всех остальных исключений
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleAllExceptions(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: ", ex);

        ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorResponse",
                new ErrorResponse("Произошла непредвиденная ошибка", "Internal Server Error", 500, request.getRequestURI()));
        return mav;
    }

    // Вспомогательный метод для определения имени view из исключения
    private String determineViewName(MethodArgumentNotValidException ex) {
        String methodName = ex.getParameter().getMethod().getName();

        switch (methodName) {
            case "register":
                return "auth/register";
            case "addBook":
            case "createBook":
                return "books/add";
            case "updateBook":
            case "editBook":
                return "books/edit";
            case "addComment":
                // Возвращаем на страницу книги
                return "redirect:/books";
            default:
                return "error";
        }
    }
}