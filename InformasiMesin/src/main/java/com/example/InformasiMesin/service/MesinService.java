package com.example.InformasiMesin.service;

import com.example.InformasiMesin.model.Mesin;
import com.example.InformasiMesin.repository.MesinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MesinService {
    @Autowired
    private MesinRepository mesinRepository;

    public List<Mesin> getAllMesin() {
        return mesinRepository.findAll();
    }

    public Mesin getMesinById(Long id) {
        return mesinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesin not found"));
    }

    public Mesin createMesin(Mesin mesin) {
        return mesinRepository.save(mesin);
    }

    public Mesin updateMesin(Long id, Mesin mesinDetails) {
        Mesin mesin = getMesinById(id);
        mesin.setEntityNo(mesinDetails.getEntityNo());
        mesin.setEntityName(mesinDetails.getEntityName());
        mesin.setBrandType(mesinDetails.getBrandType());
        mesin.setQtyGrm(mesinDetails.getQtyGrm());
        return mesinRepository.save(mesin);
    }

    public void deleteMesin(Long id) {
        mesinRepository.deleteById(id);
    }
}