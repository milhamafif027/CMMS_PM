package com.example.serverapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.serverapp.model.Mesin;
import com.example.serverapp.service.MesinService;

// MesinController.java
@RestController
@RequestMapping("/api/mesin")
@CrossOrigin(origins = "*")
public class MesinController {
    @Autowired
    private MesinService mesinService;
    
    @GetMapping
    public List<Mesin> getAllMesin() {
        return mesinService.getAllMesin();
    }
    
    @PostMapping
    public Mesin createMesin(@RequestBody Mesin mesin) {
        return mesinService.saveMesin(mesin);
    }
}
