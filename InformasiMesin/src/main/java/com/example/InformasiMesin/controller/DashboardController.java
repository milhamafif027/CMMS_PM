package com.example.InformasiMesin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("activePage", "index");
        return "index";
    }
    @GetMapping("/kalender")
    public String kalender(Model model) {
        model.addAttribute("activePage", "kalender");
        return "kalender";
    }
}
