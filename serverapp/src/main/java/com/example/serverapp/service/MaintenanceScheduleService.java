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

    // Ambil jadwal untuk laporan bulanan berdasarkan mesin
    @Transactional(readOnly = true)
    public List<MaintenanceSchedule> getMonthlyReport(Long mesinId, Integer year, String bulan) {
        List<MaintenanceSchedule> reports = maintenanceRepository.findByMesinIdAndTahunAndBulan(mesinId, year, bulan);
        if (reports.isEmpty()) {
            throw new ResourceNotFoundException("Tidak ada laporan bulanan untuk mesin ID: " + mesinId + " bulan " + bulan + " tahun " + year);
        }
        return reports;
    }

    // Ambil laporan bulanan untuk semua mesin
    @Transactional(readOnly = true)
    public List<MaintenanceSchedule> getMonthlyReport(Integer year, String month) {
        List<MaintenanceSchedule> reports = maintenanceRepository.findByTahunAndBulan(year, month);
        if (reports.isEmpty()) {
            throw new ResourceNotFoundException("Tidak ada laporan bulanan untuk bulan " + month + " tahun " + year);
        }
        return reports;
    }

    // Ambil mesin berdasarkan entity number
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
