package com.example.serverapp.repository;

import com.example.serverapp.model.MaintenanceSchedule;
import com.example.serverapp.model.Mesin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, Long> {
    List<MaintenanceSchedule> findByMesinIdAndTahun(Long mesinId, Integer tahun);

    @Query("SELECT m FROM MaintenanceSchedule m WHERE m.tahun = :year AND m.bulan = :month")
    List<MaintenanceSchedule> findByTahunAndBulan(@Param("year") Integer year, @Param("month") String month);

    @Query("SELECT m FROM MaintenanceSchedule m WHERE m.tahun = :year")
    List<MaintenanceSchedule> findByYear(@Param("year") Integer year);

    @Query("SELECT m FROM MaintenanceSchedule m WHERE m.tahun = :year AND m.bulan = :month AND m.tanggal = :date")
    List<MaintenanceSchedule> findByTahunAndBulanAndTanggal(@Param("year") Integer year, @Param("month") String month, @Param("date") Integer date);

    @Query("SELECT m FROM MaintenanceSchedule m WHERE m.tahun = :year AND m.bulan = :month AND m.tanggal = :date AND m.mesin = :mesin")
    List<MaintenanceSchedule> findByTahunAndBulanAndTanggalAndMesin(
        @Param("year") Integer year,
        @Param("month") String month,
        @Param("date") Integer date,
        @Param("mesin") Mesin mesin
);
}