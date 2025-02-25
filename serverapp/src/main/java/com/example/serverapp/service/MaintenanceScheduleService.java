package com.example.serverapp.service;

import com.example.serverapp.exception.ResourceNotFoundException;
import com.example.serverapp.model.MaintenanceSchedule;
import com.example.serverapp.model.Mesin;
import com.example.serverapp.repository.MaintenanceScheduleRepository;
import com.example.serverapp.repository.MesinRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class MaintenanceScheduleService {
    @Autowired
    private MaintenanceScheduleRepository maintenanceRepository;

    @Autowired
    private MesinRepository mesinRepository;

    // Ambil jadwal berdasarkan mesin dan tahun
    public List<MaintenanceSchedule> getScheduleByMesinAndYear(Long mesinId, Integer year) {
        return maintenanceRepository.findByMesinIdAndTahun(mesinId, year);
    }

    // Ambil jadwal untuk laporan bulanan
    public List<MaintenanceSchedule> getMonthlyReport(Long mesinId, Integer year, String bulan) {
        return maintenanceRepository.findByMesinIdAndTahunAndBulan(mesinId, year, bulan);
    }

    public Mesin findMesinByEntityNo(String entityNo) {
    return mesinRepository.findByEntityNo(entityNo)
            .orElseThrow(() -> new ResourceNotFoundException("Mesin tidak ditemukan dengan nomor entitas: " + entityNo));
    }

    // Simpan jadwal baru atau reschedule
    public MaintenanceSchedule saveSchedule(MaintenanceSchedule schedule) {
        return maintenanceRepository.save(schedule);
    }
}