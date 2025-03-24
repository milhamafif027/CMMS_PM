package com.example.serverapp.controller;

import com.example.serverapp.model.Mesin;
import com.example.serverapp.service.MesinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return mesinService.createMesin(mesin);
    }
}