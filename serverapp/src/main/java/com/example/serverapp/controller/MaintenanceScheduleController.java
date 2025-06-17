    package com.example.serverapp.controller;

    import com.example.serverapp.model.MaintenanceSchedule;
    import com.example.serverapp.model.Mesin;
    import com.example.serverapp.service.MaintenanceScheduleService;
    import com.example.serverapp.service.PdfGenerationService;
    import com.itextpdf.text.DocumentException;
    import lombok.Data;
    import org.springframework.http.HttpHeaders;
    import org.springframework.http.MediaType;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.Valid;
    import java.io.IOException;
    import java.util.List;

    @RestController
    @RequestMapping("/api/maintenance")
    @CrossOrigin(origins = "*")
    public class MaintenanceScheduleController {

        private final MaintenanceScheduleService maintenanceService;
        private final PdfGenerationService pdfGenerationService;

        public MaintenanceScheduleController(MaintenanceScheduleService maintenanceService,
                                        PdfGenerationService pdfGenerationService) {
            this.maintenanceService = maintenanceService;
            this.pdfGenerationService = pdfGenerationService;
        }

        // =============== DTO Classes ===============
        @Data
        public static class RescheduleRequest {
            @NotNull(message = "Tanggal baru tidak boleh kosong")
            private Integer newDate;
            
            @NotNull(message = "Alasan tidak boleh kosong")
            private String reason;
            
            private String maintenanceType;
            private String technician;
            
            @NotNull(message = "Tahun tidak boleh kosong")
            private Integer tahun;
        }

        @Data
        public static class CleanupRequest {
            @NotNull(message = "Tahun tidak boleh kosong")
            private Integer year;
            
            @NotNull(message = "Bulan tidak boleh kosong")
            private String month;
            
            @NotNull(message = "Tanggal mulai tidak boleh kosong")
            private Integer startDate;
            
            @NotNull(message = "Tanggal akhir tidak boleh kosong")
            private Integer endDate;
            
            @NotNull(message = "ID Mesin tidak boleh kosong")
            private Long mesinId;
        }

        // =============== CRUD Endpoints ===============
        @PostMapping
        public ResponseEntity<MaintenanceSchedule> createSchedule(
                @Valid @RequestBody MaintenanceSchedule schedule) {
            return ResponseEntity.ok(maintenanceService.saveSchedule(schedule));
        }

        @GetMapping("/{id}")
        public ResponseEntity<MaintenanceSchedule> getScheduleById(
                @PathVariable Long id) {
            return ResponseEntity.ok(maintenanceService.getScheduleById(id));
        }

        @PutMapping("/{id}")
        public ResponseEntity<MaintenanceSchedule> updateSchedule(
                @PathVariable Long id, 
                @Valid @RequestBody MaintenanceSchedule schedule) {
            schedule.setId(id);
            return ResponseEntity.ok(maintenanceService.saveSchedule(schedule));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
            maintenanceService.deleteSchedule(id);
            return ResponseEntity.noContent().build();
        }

        // =============== Query Endpoints ===============
        @GetMapping("/by-mesin-month")
        public ResponseEntity<List<MaintenanceSchedule>> getByMesinAndMonth(
                @RequestParam @NotNull Long mesinId,
                @RequestParam @NotNull Integer year,
                @RequestParam @NotNull Integer month) {
            return ResponseEntity.ok(maintenanceService.getByMesinAndMonth(mesinId, year, month));
        }

        @GetMapping("/by-mesin-date")
        public ResponseEntity<List<MaintenanceSchedule>> getByMesinAndDate(
                @RequestParam @NotNull Long mesinId,
                @RequestParam @NotNull Integer year,
                @RequestParam @NotNull Integer month,
                @RequestParam @NotNull Integer date) {
            return ResponseEntity.ok(maintenanceService.findSchedules(mesinId, year, month, date));
        }

        @GetMapping("/mesin/{mesinId}/year/{year}")
        public ResponseEntity<List<MaintenanceSchedule>> getScheduleByMesinAndYear(
                @PathVariable @NotNull Long mesinId,
                @PathVariable @NotNull Integer year) {
            return ResponseEntity.ok(maintenanceService.getScheduleByMesinAndYear(mesinId, year));
        }

        @GetMapping("/all-machines/year/{year}")
        public ResponseEntity<List<MaintenanceSchedule>> getAllSchedulesByYear(
                @PathVariable @NotNull Integer year) {
            return ResponseEntity.ok(maintenanceService.getAllSchedulesByYear(year));
        }

        // =============== Rescheduling Endpoints ===============
        @PutMapping("/reschedule/{id}")
        public ResponseEntity<MaintenanceSchedule> rescheduleMaintenance(
                @PathVariable Long id,
                @Valid @RequestBody RescheduleRequest request) {
            MaintenanceSchedule rescheduled = maintenanceService.rescheduleMaintenance(
                id,
                request.getNewDate(),
                request.getReason(),
                request.getMaintenanceType(),
                request.getTechnician(),
                request.getTahun()
            );
            return ResponseEntity.ok(rescheduled);
        }

        // =============== Automatic Scheduling Endpoints ===============
        @PostMapping("/generate-automatic")
        public ResponseEntity<String> generateAutomaticSchedules(
                @RequestParam @NotNull Integer year,
                @RequestParam @NotNull Integer month) {
            maintenanceService.generateAutomaticSchedules(year, month);
            return ResponseEntity.ok("Automatic schedules generated successfully for " + 
                                maintenanceService.getMonthName(month) + " " + year);
        }

        @DeleteMapping("/cleanup-weekly")
        public ResponseEntity<String> cleanupWeeklySchedules(
                @Valid @RequestBody CleanupRequest request) {
            int deletedCount = maintenanceService.deleteAutoSchedulesInWeek(
                request.getYear(),
                request.getMonth(),
                request.getStartDate(),
                request.getEndDate(),
                request.getMesinId()
            );
            return ResponseEntity.ok("Deleted " + deletedCount + " auto schedules in week " +
                request.getStartDate() + "-" + request.getEndDate());
        }

        @PostMapping("/cleanup-invalid")
        public ResponseEntity<String> cleanupInvalidSchedules() {
            int count = maintenanceService.cleanupInvalidSchedules();
            return ResponseEntity.ok("Cleaned up " + count + " invalid schedules");
        }

        // =============== Report Endpoints ===============
        @GetMapping("/report/monthly")
        public ResponseEntity<List<MaintenanceSchedule>> getMonthlyReport(
                @RequestParam @NotNull Integer year,
                @RequestParam @NotNull Integer month) {
            return ResponseEntity.ok(maintenanceService.getMonthlyReport(year, month));
        }

        @GetMapping("/report/monthly/pdf")
        public ResponseEntity<byte[]> downloadMonthlyReportPdf(
                @RequestParam @NotNull Integer year,
                @RequestParam @NotNull Integer month) throws IOException, DocumentException {
            
            List<MaintenanceSchedule> schedules = maintenanceService.getMonthlyReport(year, month);
            String monthName = maintenanceService.getMonthName(month);
            byte[] pdfBytes = pdfGenerationService.generateMonthlyReportPdf(schedules, monthName, year);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "laporan-bulanan-" + monthName + "-" + year + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        }

        // =============== Machine Endpoints ===============
        @GetMapping("/mesin/by-number/{entityNo}")
        public ResponseEntity<Mesin> getMesinByEntityNo(
                @PathVariable @NotNull String entityNo) {
            return ResponseEntity.ok(maintenanceService.findMesinByEntityNo(entityNo));
        }

        @GetMapping("/mesin")
        public ResponseEntity<List<Mesin>> getAllMesin() {
            return ResponseEntity.ok(maintenanceService.getAllMesin());
        }

        // =============== Utility Endpoints ===============
        @GetMapping("/month-name")
        public ResponseEntity<String> getMonthName(
                @RequestParam @NotNull Integer month) {
            return ResponseEntity.ok(maintenanceService.getMonthName(month));
        }

        // =============== Deprecated/Compatibility Endpoints ===============
        @Deprecated
        @GetMapping("/by-mesin-and-date")
        public ResponseEntity<List<MaintenanceSchedule>> getScheduleByMachineAndDate(
                @RequestParam @NotNull Long mesinId,
                @RequestParam @NotNull Integer year,
                @RequestParam @NotNull Integer month,
                @RequestParam(required = false) Integer date) {
            if (date != null) {
                return ResponseEntity.ok(maintenanceService.findSchedules(mesinId, year, month, date));
            }
            return ResponseEntity.ok(maintenanceService.getByMesinAndMonth(mesinId, year, month));
        }
    }