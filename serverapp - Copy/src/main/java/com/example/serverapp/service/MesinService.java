package com.example.serverapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.serverapp.model.Mesin;
import com.example.serverapp.repository.MesinRepository;

@Service
@Transactional
public class MesinService {
    @Autowired
    private MesinRepository mesinRepository;
    
    public List<Mesin> getAllMesin() {
        return mesinRepository.findAll();
    }
    
    public Mesin saveMesin(Mesin mesin) {
        return mesinRepository.save(mesin);
    }
}