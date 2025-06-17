package com.example.serverapp.repository;

import com.example.serverapp.model.MaintenanceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, Long> {

    @Query("SELECT m FROM MaintenanceSchedule m LEFT JOIN FETCH m.mesin WHERE m.id = :id")
    Optional<MaintenanceSchedule> findByIdWithVersion(@Param("id") Long id);

    @Query("SELECT m FROM MaintenanceSchedule m LEFT JOIN FETCH m.mesin WHERE m.mesin.id = :mesinId AND m.tahun = :tahun ORDER BY m.bulan, m.tanggal")
    List<MaintenanceSchedule> findByMesinIdAndTahun(@Param("mesinId") Long mesinId, @Param("tahun") Integer tahun);

    @Query("SELECT m FROM MaintenanceSchedule m LEFT JOIN FETCH m.mesin WHERE m.tahun = :tahun ORDER BY m.bulan, m.tanggal")
    List<MaintenanceSchedule> findByTahun(@Param("tahun") Integer tahun);

    @Query("SELECT m FROM MaintenanceSchedule m LEFT JOIN FETCH m.mesin WHERE m.tahun = :tahun AND m.bulan = :bulan ORDER BY m.tanggal")
    List<MaintenanceSchedule> findByTahunAndBulan(@Param("tahun") Integer tahun, @Param("bulan") String bulan);

    @Query("SELECT m FROM MaintenanceSchedule m LEFT JOIN FETCH m.mesin WHERE m.mesin.id = :mesinId AND m.tahun = :tahun AND m.bulan = :bulan ORDER BY m.tanggal")
    List<MaintenanceSchedule> findByMesinIdAndTahunAndBulan(@Param("mesinId") Long mesinId, @Param("tahun") Integer tahun, @Param("bulan") String bulan);

    @Query("SELECT m FROM MaintenanceSchedule m LEFT JOIN FETCH m.mesin WHERE m.mesin.id = :mesinId AND m.tahun = :tahun AND m.bulan = :bulan AND m.tanggal = :tanggal")
    List<MaintenanceSchedule> findByMesinIdAndTahunAndBulanAndTanggal(@Param("mesinId") Long mesinId, @Param("tahun") Integer tahun, @Param("bulan") String bulan, @Param("tanggal") Integer tanggal);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MaintenanceSchedule m WHERE m.tahun = :tahun AND m.bulan = :bulan AND m.tanggal = :tanggal AND m.mesin.id = :mesinId")
    boolean existsByTahunAndBulanAndTanggalAndMesinId(@Param("tahun") Integer tahun, @Param("bulan") String bulan, @Param("tanggal") Integer tanggal, @Param("mesinId") Long mesinId);

    @Modifying
    @Transactional
    @Query("DELETE FROM MaintenanceSchedule m WHERE m.tahun = :year AND m.bulan = :month AND m.isRescheduled = false AND m.action = 'Pemeliharaan Rutin'")
    int deleteAutomaticSchedulesByYearAndMonth(@Param("year") Integer year, @Param("month") String month);

    @Modifying
    @Transactional
    @Query("DELETE FROM MaintenanceSchedule m WHERE m.tahun = :year AND m.bulan = :month AND m.tanggal BETWEEN :startDate AND :endDate AND m.mesin.id = :mesinId AND m.isRescheduled = false AND m.action = 'Pemeliharaan Rutin'")
    int deleteAutoSchedulesInWeek(@Param("year") Integer year, @Param("month") String month, @Param("startDate") Integer startDate, @Param("endDate") Integer endDate, @Param("mesinId") Long mesinId);

    @Modifying
    @Transactional
    @Query("DELETE FROM MaintenanceSchedule m WHERE m.tahun = :tahun AND m.bulan = :bulan AND m.tanggal IN :dates AND m.mesin.id = :mesinId AND m.isRescheduled = false AND m.action = 'Pemeliharaan Rutin'")
    void deleteRoutineSchedulesByDates(@Param("tahun") Integer tahun, @Param("bulan") String bulan, @Param("dates") List<Integer> dates, @Param("mesinId") Long mesinId);

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM MaintenanceSchedule m
        WHERE m.tahun = :tahun
        AND m.bulan = :bulan
        AND m.tanggal IN :dates
        AND m.mesin.id = :mesinId
        AND m.id NOT IN :excludedIds
        AND m.isRescheduled = false
        AND LOWER(TRIM(m.action)) = LOWER('Pemeliharaan Rutin')
    """)
    int deleteRoutineSchedulesByDatesExceptIds(
        @Param("tahun") Integer tahun,
        @Param("bulan") String bulan,
        @Param("dates") List<Integer> dates,
        @Param("mesinId") Long mesinId,
        @Param("excludedIds") List<Long> excludedIds
    );    
    

    @Query("SELECT m FROM MaintenanceSchedule m LEFT JOIN FETCH m.mesin WHERE m.tahun = :year AND m.bulan = :bulan AND m.mesin.id = :mesinId AND m.tanggal IN :tanggal")
    List<MaintenanceSchedule> findByTahunAndBulanAndMesinIdAndTanggalIn(@Param("tahun") Integer tahun, @Param("bulan") String bulan, @Param("mesinId") Long mesinId, @Param("tanggal") List<Integer> tanggal);

    @Query("SELECT m FROM MaintenanceSchedule m LEFT JOIN FETCH m.mesin WHERE m.tahun = :year AND m.bulan = :month AND m.status IN ('Dijadwalkan', 'Rescheduled') ORDER BY m.tanggal")
    List<MaintenanceSchedule> findSchedulesNeedingAttention(@Param("year") Integer year, @Param("month") String month);

    @Query("SELECT m FROM MaintenanceSchedule m LEFT JOIN FETCH m.mesin WHERE m.status = :status ORDER BY m.tahun, m.bulan, m.tanggal")
    List<MaintenanceSchedule> findByStatus(@Param("status") String status);

    @Query("SELECT m FROM MaintenanceSchedule m LEFT JOIN FETCH m.mesin WHERE m.isRescheduled = true ORDER BY m.tahun, m.bulan, m.tanggal")
    List<MaintenanceSchedule> findByIsRescheduledTrue();

    @Query("SELECT DISTINCT m.tahun FROM MaintenanceSchedule m ORDER BY m.tahun DESC")
    List<Integer> findDistinctYears();

    @Query("SELECT DISTINCT m.bulan FROM MaintenanceSchedule m WHERE m.tahun = :tahun ORDER BY CASE m.bulan WHEN 'Januari' THEN 1 WHEN 'Februari' THEN 2 WHEN 'Maret' THEN 3 WHEN 'April' THEN 4 WHEN 'Mei' THEN 5 WHEN 'Juni' THEN 6 WHEN 'Juli' THEN 7 WHEN 'Agustus' THEN 8 WHEN 'September' THEN 9 WHEN 'Oktober' THEN 10 WHEN 'November' THEN 11 WHEN 'Desember' THEN 12 END")
    List<String> findDistinctMonthsByYear(@Param("tahun") Integer tahun);

    @Query("SELECT m.status, COUNT(m) FROM MaintenanceSchedule m GROUP BY m.status")
    List<Object[]> countSchedulesByStatus();

    default Map<String, Long> getStatusCounts() {
        return countSchedulesByStatus().stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));
    }

    @Query("SELECT m.bulan, COUNT(m) as jumlah FROM MaintenanceSchedule m WHERE m.isRescheduled = false AND m.action = 'Pemeliharaan Rutin' GROUP BY m.bulan ORDER BY FUNCTION('MONTH_ORDER', m.bulan)")
    List<Object[]> countRoutineSchedulesByMonth();

    @Query("SELECT FUNCTION('MONTH_ORDER', m.bulan) FROM MaintenanceSchedule m")
    int getMonthOrder(String bulan);

    @Query("SELECT m FROM MaintenanceSchedule m LEFT JOIN FETCH m.mesin WHERE m.tahun = :year AND m.bulan = :month AND m.tanggal BETWEEN :startDate AND :endDate AND m.mesin.id = :mesinId AND m.isRescheduled = false AND m.action = 'Pemeliharaan Rutin'")
    List<MaintenanceSchedule> findAutomaticSchedulesInWeek(@Param("year") Integer year, @Param("month") String month, @Param("startDate") Integer startDate, @Param("endDate") Integer endDate, @Param("mesinId") Long mesinId);
}
