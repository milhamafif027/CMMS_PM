package com.example.serverapp.service;

import com.example.serverapp.model.MaintenanceSchedule;
import com.example.serverapp.model.Mesin;
import com.example.serverapp.repository.MesinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class MesinService {

    @Autowired
    private MesinRepository mesinRepository;

    @Autowired
    private MaintenanceScheduleService maintenanceScheduleService;

    @Transactional(readOnly = true)
    public List<Mesin> getAllMesin() {
        return mesinRepository.findAll();
    }

    @Transactional
    public Mesin createMesin(Mesin mesin) {
        Mesin savedMesin = mesinRepository.save(mesin);
        generateMaintenanceSchedules(savedMesin);
        return savedMesin;
    }

    private void generateMaintenanceSchedules(Mesin mesin) {
        int currentYear = LocalDate.now().getYear();
        String[] months = {"Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"};

        for (String month : months) {
            MaintenanceSchedule schedule7 = new MaintenanceSchedule();
            schedule7.setMesin(mesin);
            schedule7.setBulan(month);
            schedule7.setTanggal(7);
            schedule7.setTahun(currentYear);
            schedule7.setAction("Penggantian Oli");
            schedule7.setStatus("Dijadwalkan");
            schedule7.setTechnician("Teknisi Default");
            schedule7.setDescription("Maintenance rutin penggantian oli");
            maintenanceScheduleService.saveSchedule(schedule7);

            MaintenanceSchedule schedule14 = new MaintenanceSchedule();
            schedule14.setMesin(mesin);
            schedule14.setBulan(month);
            schedule14.setTanggal(14);
            schedule14.setTahun(currentYear);
            schedule14.setAction("Penggantian Oli");
            schedule14.setStatus("Dijadwalkan");
            schedule14.setTechnician("Teknisi Default");
            schedule14.setDescription("Maintenance rutin penggantian oli");
            maintenanceScheduleService.saveSchedule(schedule14);
        }
    }
}