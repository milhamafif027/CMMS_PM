package com.example.InformasiMesin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin() {
        // Spring Security akan menangani proses login
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String handleLogout() {
        // Spring Security akan menangani proses logout
        return "redirect:/login";
    }
}