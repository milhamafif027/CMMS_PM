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
public class MaintenanceScheduleService {

    @Autowired
    private MaintenanceScheduleRepository maintenanceRepository;

    @Autowired
    private MesinRepository mesinRepository;

    // Ambil jadwal berdasarkan mesin dan tahun
    @Transactional(readOnly = true)
    public List<MaintenanceSchedule> getScheduleByMesinAndYear(Long mesinId, Integer year) {
        List<MaintenanceSchedule> schedules = maintenanceRepository.findByMesinIdAndTahun(mesinId, year);
        if (schedules.isEmpty()) {
            throw new ResourceNotFoundException("Tidak ada jadwal perawatan untuk mesin ID: " + mesinId + " di tahun " + year);
        }
        return schedules;
    }

    // Ambil laporan bulanan berdasarkan tahun dan nama bulan
    @Transactional(readOnly = true)
    public List<MaintenanceSchedule> getMonthlyReport(Integer year, String month) {
        List<MaintenanceSchedule> reports = maintenanceRepository.findByTahunAndBulan(year, month);
        if (reports.isEmpty()) {
            throw new ResourceNotFoundException("Tidak ada laporan bulanan untuk bulan " + month + " tahun " + year);
        }
        return reports;
    }

    // Konversi angka bulan menjadi nama bulan
    public String getMonthName(Integer month) {
        String[] months = {
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        };
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Bulan tidak valid: " + month);
        }
        return months[month - 1];
    }

    // Ambil mesin berdasarkan nomor entitas
    @Transactional(readOnly = true)
    public Mesin findMesinByEntityNo(String entityNo) {
        return mesinRepository.findByEntityNo(entityNo)
                .orElseThrow(() -> new ResourceNotFoundException("Mesin tidak ditemukan dengan nomor entitas: " + entityNo));
    }

    // Simpan jadwal baru atau reschedule
    @Transactional
    public MaintenanceSchedule saveSchedule(MaintenanceSchedule schedule) {
        return maintenanceRepository.save(schedule);
    }

    // Ambil semua jadwal berdasarkan tahun
    @Transactional(readOnly = true)
    public List<MaintenanceSchedule> getAllSchedulesByYear(Integer year) {
        List<MaintenanceSchedule> schedules = maintenanceRepository.findByYear(year);
        if (schedules.isEmpty()) {
            throw new ResourceNotFoundException("Tidak ada jadwal perawatan untuk tahun " + year);
        }
        return schedules;
    }

    // Ambil semua mesin yang tersedia
    @Transactional(readOnly = true)
    public List<Mesin> getAllMesin() {
        List<Mesin> mesinList = mesinRepository.findAll();
        if (mesinList.isEmpty()) {
            throw new ResourceNotFoundException("Tidak ada mesin yang tersedia.");
        }
        return mesinList;
    }
}
