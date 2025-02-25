package com.example.InformasiMesin.service;

import com.example.InformasiMesin.model.MaintenanceSchedule;
import com.example.InformasiMesin.repository.MaintenanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MaintenanceService {
    @Autowired
    private MaintenanceRepository maintenanceRepository;

    public List<MaintenanceSchedule> getScheduleByMesinAndYear(Long mesinId, Integer year) {
        return maintenanceRepository.findByMesinIdAndTahun(mesinId, year);
    }

    public MaintenanceSchedule updateSchedule(Long id, MaintenanceSchedule schedule) {
        MaintenanceSchedule existing = maintenanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        existing.setStatus(schedule.getStatus());
        existing.setAction(schedule.getAction());
        return maintenanceRepository.save(existing);
    }
}