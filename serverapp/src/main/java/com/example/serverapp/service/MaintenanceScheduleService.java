package com.example.serverapp.service;

import com.example.serverapp.exception.ResourceNotFoundException;
import com.example.serverapp.model.MaintenanceSchedule;
import com.example.serverapp.model.Mesin;
import com.example.serverapp.repository.MaintenanceScheduleRepository;
import com.example.serverapp.repository.MesinRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaintenanceScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(MaintenanceScheduleService.class);
    private final MaintenanceScheduleRepository maintenanceRepository;
    private final MesinRepository mesinRepository;

    private static final String ROUTINE_MAINTENANCE = "Pemeliharaan Rutin";
    private static final String SCHEDULED_STATUS = "Dijadwalkan";
    private static final String RESCHEDULED_STATUS = "Rescheduled";
    private static final String REPLACED_STATUS = "Replaced";
    private static final int[] ROUTINE_DATES = {7, 14, 21, 28};

    @Transactional
    public MaintenanceSchedule saveSchedule(MaintenanceSchedule schedule) {
        validateSchedule(schedule);
        schedule.setMesin(getExistingMachine(schedule.getMesin().getId()));
        schedule.setBulan(normalizeMonth(schedule.getBulan()));
        schedule.setAction(Optional.ofNullable(schedule.getAction()).orElse(ROUTINE_MAINTENANCE).trim());
        schedule.setStatus(Optional.ofNullable(schedule.getStatus()).orElse(SCHEDULED_STATUS));
        schedule.setVersion(Optional.ofNullable(schedule.getVersion()).orElse(0));
        return maintenanceRepository.save(schedule);
    }

    @Transactional(readOnly = true)
    public MaintenanceSchedule getScheduleById(Long id) {
        return maintenanceRepository.findByIdWithVersion(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with ID: " + id));
    }

    @Transactional
    public void deleteSchedule(Long id) {
        if (!maintenanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Schedule not found with ID: " + id);
        }
        maintenanceRepository.deleteById(id);
    }

    private void validateMesinExists(Long mesinId) {
        if (mesinId == null || !mesinRepository.existsById(mesinId)) {
            throw new ResourceNotFoundException("Mesin tidak ditemukan dengan ID: " + mesinId);
        }
    }

    @Transactional(readOnly = true)
    public List<MaintenanceSchedule> getScheduleByMesinAndYear(Long mesinId, Integer year) {
        validateYear(year);
        validateMesinExists(mesinId);
        return maintenanceRepository.findByMesinIdAndTahun(mesinId, year);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceSchedule> getByMesinAndMonth(Long mesinId, Integer year, Integer month) {
        validateYear(year);
        validateMonth(month);
        validateMesinExists(mesinId);
        String monthName = normalizeMonth(getMonthName(month));
        return maintenanceRepository.findByMesinIdAndTahunAndBulan(mesinId, year, monthName);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceSchedule> findSchedules(Long mesinId, Integer year, Integer month, Integer date) {
        validateYear(year);
        validateMonth(month);
        validateDate(date, month, year);
        validateMesinExists(mesinId);
        String monthName = normalizeMonth(getMonthName(month));
        return maintenanceRepository.findByMesinIdAndTahunAndBulanAndTanggal(mesinId, year, monthName, date);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceSchedule> getAllSchedulesByYear(Integer year) {
        validateYear(year);
        return maintenanceRepository.findByTahun(year);
    }

    @Transactional
    public int generateAutomaticSchedules(Integer year, Integer month) {
        validateYear(year);
        validateMonth(month);
        String monthName = normalizeMonth(getMonthName(month));

        int deletedCount = maintenanceRepository.deleteAutomaticSchedulesByYearAndMonth(year, monthName);
        logger.info("Deleted {} existing automatic schedules", deletedCount);

        List<Mesin> machines = mesinRepository.findAll();
        if (machines.isEmpty()) {
            logger.warn("No machines found for automatic scheduling");
            return 0;
        }

        List<MaintenanceSchedule> newSchedules = new ArrayList<>();
        for (Mesin machine : machines) {
            for (int date : ROUTINE_DATES) {
                if (!existsScheduleForDate(year, monthName, date, machine.getId())) {
                    MaintenanceSchedule schedule = buildAutomaticSchedule(year, monthName, date, machine);
                    newSchedules.add(schedule);
                }
            }
        }

        maintenanceRepository.saveAll(newSchedules);
        logger.info("Created {} new automatic schedules", newSchedules.size());
        return newSchedules.size();
    }

    @CacheEvict(value = "monthlyReports", allEntries = true)
    @Transactional
    public MaintenanceSchedule rescheduleMaintenance(Long id, Integer newDate, String reason, String maintenanceType, String technician, Integer tahun) {
        // Ambil jadwal lama
        MaintenanceSchedule originalSchedule = maintenanceRepository.findByIdWithVersion(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with ID: " + id));
    
        // Normalisasi bulan
        String normalizedBulan = normalizeMonth(originalSchedule.getBulan());
    
        // Validasi tanggal baru
        validateRescheduleDate(newDate, normalizedBulan, tahun);
    
        // Cek bentrok jadwal di tanggal baru
        if (existsScheduleForDate(tahun, normalizedBulan, newDate, originalSchedule.getMesin().getId())) {
            throw new IllegalArgumentException("Schedule already exists for this machine on the new date.");
        }
    
        // Tandai jadwal lama sebagai 'digantikan'
        originalSchedule.setStatus(REPLACED_STATUS);
        maintenanceRepository.save(originalSchedule);
    
        // Buat jadwal baru hasil reschedule
        MaintenanceSchedule rescheduled = MaintenanceSchedule.builder()
                .bulan(normalizedBulan)
                .tanggal(newDate)
                .tahun(tahun)
                .action(Optional.ofNullable(maintenanceType).orElse(originalSchedule.getAction()).trim())
                .status(RESCHEDULED_STATUS)
                .technician(Optional.ofNullable(technician).orElse(originalSchedule.getTechnician()))
                .description(originalSchedule.getDescription())
                .isRescheduled(true)
                .rescheduleReason(reason)
                .mesin(originalSchedule.getMesin())
                .version(0) // Set default version
                .build();
    
        MaintenanceSchedule savedReschedule = maintenanceRepository.save(rescheduled);
    
        // Perhitungan minggu dari tanggal baru
        int weekStart = getStartOfWeek(newDate);
        int weekEnd = getEndOfWeek(newDate);
    
        List<Integer> routineDatesInWeek = Arrays.stream(ROUTINE_DATES)
                .filter(date -> date >= weekStart && date <= weekEnd)
                .boxed()
                .collect(Collectors.toList());
    
        // Jika ada jadwal rutin di minggu ini, hapus selain reschedule baru
        if (!routineDatesInWeek.isEmpty()) {
            List<Long> excludedIds = List.of(savedReschedule.getId());
    
            logger.info("Attempting to delete routine schedules for mesin ID {} in {} {}, excluding IDs: {}",
                    originalSchedule.getMesin().getId(), normalizedBulan, tahun, excludedIds);
    
            int deleted = maintenanceRepository.deleteRoutineSchedulesByDatesExceptIds(
                    tahun,
                    normalizedBulan,
                    routineDatesInWeek,
                    originalSchedule.getMesin().getId(),
                    excludedIds
            );
    
            logger.info("✅ Deleted {} routine schedules.", deleted);
        }
    
        return savedReschedule;
    }
    
    @Cacheable("monthlyReports")
    @Transactional(readOnly = true)
    public List<MaintenanceSchedule> getMonthlyReport(Integer year, Integer month) {
        validateYear(year);
        validateMonth(month);
        String monthName = normalizeMonth(getMonthName(month));
        List<MaintenanceSchedule> schedules = maintenanceRepository.findByTahunAndBulan(year, monthName);

        if (schedules.isEmpty()) {
            throw new ResourceNotFoundException("No schedules found for " + monthName + " " + year);
        }

        return schedules;
    }

    @Transactional
    public int deleteAutoSchedulesInWeek(Integer year, String month, Integer startDate, Integer endDate, Long mesinId) {
        List<MaintenanceSchedule> schedules = maintenanceRepository.findAutomaticSchedulesInWeek(
                year, normalizeMonth(month), startDate, endDate, mesinId);
        maintenanceRepository.deleteAll(schedules);
        return schedules.size();
    }

    @Transactional
    public int cleanupInvalidSchedules() {
        List<MaintenanceSchedule> invalidSchedules = maintenanceRepository.findAll().stream()
                .filter(schedule -> schedule.getMesin() == null || schedule.getMesin().getId() == null)
                .collect(Collectors.toList());

        if (!invalidSchedules.isEmpty()) {
            maintenanceRepository.deleteAll(invalidSchedules);
        }
        return invalidSchedules.size();
    }

    @Transactional(readOnly = true)
    public Mesin findMesinByEntityNo(String entityNo) {
        return mesinRepository.findByEntityNo(entityNo)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found with entity number: " + entityNo));
    }

    @Transactional(readOnly = true)
    public List<Mesin> getAllMesin() {
        return mesinRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<MaintenanceSchedule> getUpcomingMaintenance(int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(daysAhead);
        String monthName = normalizeMonth(getMonthName(today.getMonthValue()));

        return maintenanceRepository.findSchedulesNeedingAttention(today.getYear(), monthName).stream()
                .filter(schedule -> {
                    LocalDate scheduleDate = LocalDate.of(
                            schedule.getTahun(),
                            Arrays.asList(getMonthNames()).indexOf(schedule.getBulan()) + 1,
                            schedule.getTanggal()
                    );
                    return !scheduleDate.isBefore(today) && !scheduleDate.isAfter(endDate);
                })
                .collect(Collectors.toList());
    }

    private boolean existsScheduleForDate(Integer year, String month, Integer date, Long mesinId) {
        return maintenanceRepository.existsByTahunAndBulanAndTanggalAndMesinId(
                year, normalizeMonth(month), date, mesinId);
    }

    private void validateSchedule(MaintenanceSchedule schedule) {
        if (schedule == null) throw new IllegalArgumentException("Schedule cannot be null");
        validateMachine(schedule.getMesin());
        validateDate(schedule.getTanggal(), schedule.getBulan(), schedule.getTahun());
    }

    private void validateMachine(Mesin mesin) {
        if (mesin == null || mesin.getId() == null) throw new IllegalArgumentException("Invalid machine data");
        if (!mesinRepository.existsById(mesin.getId())) {
            throw new ResourceNotFoundException("Machine not found with ID: " + mesin.getId());
        }
    }

    private Mesin getExistingMachine(Long mesinId) {
        return mesinRepository.findById(mesinId)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found with ID: " + mesinId));
    }

    private void validateRescheduleDate(Integer date, String monthName, Integer year) {
        validateDate(date, monthName, year);
    }

    private void validateDate(Integer date, String monthName, Integer year) {
        if (date == null || date < 1) throw new IllegalArgumentException("Invalid date: " + date);
        int maxDays = getMaxDaysInMonth(monthName, year);
        if (date > maxDays) {
            throw new IllegalArgumentException("Date exceeds maximum days (" + maxDays + ") in " + monthName);
        }
    }

    private void validateDate(Integer date, Integer month, Integer year) {
        validateDate(date, getMonthName(month), year);
    }

    private void validateMonth(Integer month) {
        if (month == null || month < 1 || month > 12) throw new IllegalArgumentException("Month must be between 1-12");
    }

    private void validateYear(Integer year) {
        if (year == null || year < 2000 || year > 2100) throw new IllegalArgumentException("Year must be between 2000-2100");
    }

    public String getMonthName(Integer month) {
        if (month < 1 || month > 12) throw new IllegalArgumentException("Invalid month: " + month);
        return getMonthNames()[month - 1];
    }

    private String[] getMonthNames() {
        return new String[]{
                "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        };
    }

    private int getMaxDaysInMonth(String month, int year) {
        return switch (month) {
            case "Februari" -> isLeapYear(year) ? 29 : 28;
            case "April", "Juni", "September", "November" -> 30;
            default -> 31;
        };
    }

    private boolean isLeapYear(int year) {
        return (year % 400 == 0) || (year % 100 != 0 && year % 4 == 0);
    }

    private int getStartOfWeek(Integer date) {
        return ((date - 1) / 7) * 7 + 1;
    }

    private int getEndOfWeek(Integer date) {
        return ((date - 1) / 7 + 1) * 7;
    }

    private String normalizeMonth(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    private MaintenanceSchedule buildAutomaticSchedule(Integer year, String monthName, int date, Mesin mesin) {
        return MaintenanceSchedule.builder()
                .bulan(normalizeMonth(monthName))
                .tanggal(date)
                .tahun(year)
                .action(ROUTINE_MAINTENANCE)
                .status(SCHEDULED_STATUS)
                .technician("Teknisi Default")
                .description("Jadwal pemeliharaan rutin")
                .isRescheduled(false)
                .rescheduleReason(null)
                .mesin(mesin)
                .version(0)
                .build();
    }

    @Scheduled(cron = "0 0 1 1 * ?")
    @Transactional
    public void autoGenerateMonthlySchedules() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int result = generateAutomaticSchedules(year, month);
        logger.info("Auto-generated {} schedules for {} {}", result, getMonthName(month), year);
    }

    @PostConstruct
    public void generateOnStartup() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        String monthName = getMonthName(month);
        logger.info("\uD83D\uDE80 Startup check: ensuring routine schedules exist for {} {}", monthName, year);

        List<Mesin> mesinList = mesinRepository.findAll();

        if (mesinList.isEmpty()) {
            logger.warn("\u26A0\uFE0F Tidak ada mesin terdaftar. Melewati generate jadwal rutin pada startup.");
            return;
        }

        boolean hasRoutineSchedules = mesinList.stream().anyMatch(mesin ->
                Arrays.stream(ROUTINE_DATES).anyMatch(date ->
                        existsScheduleForDate(year, monthName, date, mesin.getId())
                )
        );

        if (hasRoutineSchedules) {
            logger.info("\u23E9 Jadwal rutin untuk {} {} sudah ada. Tidak perlu generate ulang.", monthName, year);
            return;
        }

        int created = generateAutomaticSchedules(year, month);
        logger.info("\u2705 Jadwal rutin berhasil dibuat saat startup: {} jadwal untuk {} {}", created, monthName, year);
    }
}
