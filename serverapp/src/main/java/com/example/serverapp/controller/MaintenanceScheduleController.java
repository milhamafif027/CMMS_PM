package com.example.serverapp.controller;

import com.example.serverapp.model.MaintenanceSchedule;
import com.example.serverapp.model.Mesin;
import com.example.serverapp.service.MaintenanceScheduleService;
import com.example.serverapp.service.PdfGenerationService;
import com.itextpdf.text.DocumentException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
@CrossOrigin(origins = "*")
public class MaintenanceScheduleController {

    private final MaintenanceScheduleService maintenanceService;
    private final PdfGenerationService pdfGenerationService;

    public MaintenanceScheduleController(MaintenanceScheduleService maintenanceService, PdfGenerationService pdfGenerationService) {
        this.maintenanceService = maintenanceService;
        this.pdfGenerationService = pdfGenerationService;
    }

    @GetMapping("/mesin/{mesinId}/year/{year}")
    public ResponseEntity<List<MaintenanceSchedule>> getSchedule(@PathVariable Long mesinId, @PathVariable Integer year) {
        return ResponseEntity.ok(maintenanceService.getScheduleByMesinAndYear(mesinId, year));
    }

    @GetMapping("/all-machines/year/{year}")
    public ResponseEntity<List<MaintenanceSchedule>> getAllSchedulesByYear(@PathVariable Integer year) {
        return ResponseEntity.ok(maintenanceService.getAllSchedulesByYear(year));
    }

    @GetMapping("/mesin/by-number/{entityNo}")
    public ResponseEntity<Mesin> getMesinByEntityNo(@PathVariable String entityNo) {
        return ResponseEntity.ok(maintenanceService.findMesinByEntityNo(entityNo));
    }

    @GetMapping("/mesin")
    public ResponseEntity<List<Mesin>> getAllMesin() {
        return ResponseEntity.ok(maintenanceService.getAllMesin());
    }

    @PostMapping
    public ResponseEntity<MaintenanceSchedule> createSchedule(@RequestBody MaintenanceSchedule schedule) {
        MaintenanceSchedule savedSchedule = maintenanceService.saveSchedule(schedule);
        return ResponseEntity.ok(savedSchedule);
    }

    @PutMapping("/reschedule/{id}")
    public ResponseEntity<MaintenanceSchedule> rescheduleMaintenance(
            @PathVariable Long id,
            @RequestParam Integer newDate,
            @RequestParam String reason) {
        return ResponseEntity.ok(maintenanceService.rescheduleMaintenance(id, newDate, reason));
    }

    @GetMapping("/report/monthly")
    public ResponseEntity<List<MaintenanceSchedule>> getMonthlyReport(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        String monthName = maintenanceService.getMonthName(month);
        return ResponseEntity.ok(maintenanceService.getMonthlyReport(year, monthName));
    }

    @GetMapping("/report/monthly/pdf")
    public ResponseEntity<byte[]> downloadMonthlyReportPdf(
            @RequestParam Integer year,
            @RequestParam Integer month) throws IOException, DocumentException {
        String monthName = maintenanceService.getMonthName(month);
        List<MaintenanceSchedule> schedules = maintenanceService.getMonthlyReport(year, monthName);
        byte[] pdfBytes = pdfGenerationService.generateMonthlyReportPdf(schedules, monthName, year);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "laporan-bulanan-" + monthName + "-" + year + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/automatic-schedule")
    public ResponseEntity<String> generateAutomaticSchedule(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        maintenanceService.saveAutomaticSchedule(year, month);
        return ResponseEntity.ok("Jadwal otomatis berhasil dibuat");
    }

    @GetMapping("/cleanup")
    public ResponseEntity<String> cleanupSchedules() {
        maintenanceService.cleanupInvalidSchedules();
        return ResponseEntity.ok("Pembersihan jadwal tidak valid selesai");
    }
}