package com.example.bookreview.controller;

import com.example.bookreview.dto.UserRegisterDto;
import com.example.bookreview.entity.User;
import com.example.bookreview.exception.BusinessException;
import com.example.bookreview.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("userRegisterDto", new UserRegisterDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute UserRegisterDto dto, BindingResult result) {
        if (result.hasErrors()) {
            return "auth/register";
        }

        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new BusinessException("Имя пользователя уже занято");
        }

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new BusinessException("Email уже используется");
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("Пароли не совпадают");
        }

        if (!isPasswordValid(dto.getPassword())) {
            throw new BusinessException("Пароль должен содержать минимум 6 символов, включая буквы и цифры");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole("ROLE_USER");
        userRepository.save(user);

        return "redirect:/login?success";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "success", required = false) String success,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Неверное имя пользователя или пароль");
        }
        if (logout != null) {
            model.addAttribute("message", "Вы успешно вышли из системы");
        }
        if (success != null) {
            model.addAttribute("message", "Регистрация прошла успешно! Теперь вы можете войти.");
        }
        return "auth/login";
    }

    private boolean isPasswordValid(String password) {
        // Проверяем что пароль содержит минимум одну букву и одну цифру
        return password.matches(".*[a-zA-Z].*") && password.matches(".*\\d.*");
    }
}