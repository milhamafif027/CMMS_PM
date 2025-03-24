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

    private final MaintenanceScheduleRepository maintenanceRepository;
    private final MesinRepository mesinRepository;

    @Autowired
    public MaintenanceScheduleService(MaintenanceScheduleRepository maintenanceRepository, MesinRepository mesinRepository) {
        this.maintenanceRepository = maintenanceRepository;
        this.mesinRepository = mesinRepository;
    }

    @Transactional(readOnly = true)
    public List<MaintenanceSchedule> getScheduleByMesinAndYear(Long mesinId, Integer year) {
        return maintenanceRepository.findByMesinIdAndTahun(mesinId, year);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceSchedule> getMonthlyReport(Integer year, String month) {
        List<MaintenanceSchedule> schedules = maintenanceRepository.findByTahunAndBulan(year, month);
        if (schedules.isEmpty()) {
            throw new ResourceNotFoundException("Tidak ada jadwal ditemukan untuk bulan: " + month + " tahun: " + year);
        }
        return schedules;
    }

    @Transactional(readOnly = true)
    public List<MaintenanceSchedule> getAllSchedulesByYear(Integer year) {
        List<MaintenanceSchedule> schedules = maintenanceRepository.findByYear(year);
        if (schedules.isEmpty()) {
            throw new ResourceNotFoundException("Tidak ada jadwal ditemukan untuk tahun: " + year);
        }
        return schedules;
    }

    @Transactional
    public MaintenanceSchedule saveSchedule(MaintenanceSchedule schedule) {
        // Pastikan mesin sudah ada di database
        Mesin mesin = schedule.getMesin();
        if (mesin == null || mesin.getId() == null) {
            throw new IllegalArgumentException("Mesin tidak valid");
        }

        // Cari mesin di database
        Mesin existingMesin = mesinRepository.findById(mesin.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Mesin tidak ditemukan dengan ID: " + mesin.getId()));

        // Set mesin ke jadwal
        schedule.setMesin(existingMesin);

        // Simpan jadwal
        return maintenanceRepository.save(schedule);
    }

@Transactional
public void saveAutomaticSchedule(Integer year, Integer month) {
    String monthName = getMonthName(month);
    int[] dates = {7, 14, 21, 28}; // Tanggal yang dijadwalkan

    // Ambil semua mesin dari database
    List<Mesin> mesinList = mesinRepository.findAll();
    if (mesinList.isEmpty()) {
        throw new ResourceNotFoundException("Tidak ada mesin ditemukan");
    }

    // Loop melalui setiap tanggal
    for (int date : dates) {
        // Loop melalui setiap mesin
        for (Mesin mesin : mesinList) {
            // Cek apakah jadwal sudah ada untuk mesin ini pada tanggal ini
            List<MaintenanceSchedule> existingSchedules = maintenanceRepository.findByTahunAndBulanAndTanggalAndMesin(year, monthName, date, mesin);
            if (!existingSchedules.isEmpty()) {
                continue; // Lewati jika jadwal sudah ada
            }

            // Buat jadwal baru
            MaintenanceSchedule schedule = new MaintenanceSchedule();
            schedule.setBulan(monthName);
            schedule.setTanggal(date);
            schedule.setTahun(year);
            schedule.setAction("Pemeliharaan Rutin");
            schedule.setStatus("Dijadwalkan");
            schedule.setTechnician("Teknisi Default");
            schedule.setDescription("Jadwal pemeliharaan rutin");
            schedule.setIsRescheduled(false);
            schedule.setRescheduleReason(null);
            schedule.setMesin(mesin); // Gunakan mesin yang sedang diproses

            // Simpan jadwal
            maintenanceRepository.save(schedule);
        }
    }
}

    @Transactional
    public void cleanupInvalidSchedules() {
        List<MaintenanceSchedule> schedules = maintenanceRepository.findAll();
        for (MaintenanceSchedule schedule : schedules) {
            if (schedule.getMesin() == null || schedule.getMesin().getId() == null) {
                maintenanceRepository.delete(schedule); // Hapus jadwal yang tidak valid
            }
        }
    }

    @Transactional
    public MaintenanceSchedule rescheduleMaintenance(Long id, Integer newDate, String reason) {
        MaintenanceSchedule schedule = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jadwal tidak ditemukan dengan ID: " + id));

        schedule.setTanggal(newDate);
        schedule.setIsRescheduled(true);
        schedule.setRescheduleReason(reason);
        schedule.setStatus("Rescheduled");

        return maintenanceRepository.save(schedule);
    }

    public String getMonthName(Integer month) {
        String[] months = {"Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Bulan tidak valid: " + month);
        }
        return months[month - 1];
    }

    @Transactional(readOnly = true)
    public Mesin findMesinByEntityNo(String entityNo) {
        System.out.println("Searching for mesin with entityNo: " + entityNo); // Logging untuk memeriksa nomor entitas
        return mesinRepository.findByEntityNo(entityNo)
                .orElseThrow(() -> new ResourceNotFoundException("Mesin tidak ditemukan dengan nomor entitas: " + entityNo));
    }

    @Transactional(readOnly = true)
    public List<Mesin> getAllMesin() {
        return mesinRepository.findAll();
    }
}