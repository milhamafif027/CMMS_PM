package com.example.serverapp.controller;

import com.example.serverapp.model.MaintenanceSchedule;
import com.example.serverapp.model.Mesin;
import com.example.serverapp.service.MaintenanceScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
@CrossOrigin(origins = "*")
public class MaintenanceController {
    @Autowired
    private MaintenanceScheduleService maintenanceService;

    // Ambil jadwal berdasarkan mesin dan tahun
    @GetMapping("/mesin/{mesinId}/year/{year}")
    public List<MaintenanceSchedule> getSchedule(@PathVariable Long mesinId, @PathVariable Integer year) {
        return maintenanceService.getScheduleByMesinAndYear(mesinId, year);
    }

    // Ambil laporan bulanan
    @GetMapping("/mesin/{mesinId}/year/{year}/month/{bulan}")
    public List<MaintenanceSchedule> getMonthlyReport(
            @PathVariable Long mesinId,
            @PathVariable Integer year,
            @PathVariable String bulan) {
        return maintenanceService.getMonthlyReport(mesinId, year, bulan);
    }

    @GetMapping("/all-machines/year/{year}")
    public List<MaintenanceSchedule> getAllSchedulesByYear(@PathVariable Integer year) {
        return maintenanceService.getAllSchedulesByYear(year);
    }

    @GetMapping("/mesin/by-number/{entityNo}")
    public Mesin getMesinByEntityNo(@PathVariable String entityNo) {
        return maintenanceService.findMesinByEntityNo(entityNo);
    }

    @GetMapping("/mesin")
    public List<Mesin> getAllMesin() {
        return maintenanceService.getAllMesin();
    }

    // Simpan jadwal baru atau reschedule
    @PostMapping
    public MaintenanceSchedule createSchedule(@RequestBody MaintenanceSchedule schedule) {
        return maintenanceService.saveSchedule(schedule);
    }
}