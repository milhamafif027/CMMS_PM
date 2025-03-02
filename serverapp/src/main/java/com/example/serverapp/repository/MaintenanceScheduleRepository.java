package com.example.serverapp.repository;

import com.example.serverapp.model.MaintenanceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, Long> {

    // Cari jadwal berdasarkan mesin dan tahun
    List<MaintenanceSchedule> findByMesinIdAndTahun(Long mesinId, Integer tahun);

    // Cari laporan bulanan berdasarkan tahun dan nama bulan
    @Query("SELECT m FROM MaintenanceSchedule m WHERE m.tahun = :year AND m.bulan = :month")
    List<MaintenanceSchedule> findByTahunAndBulan(@Param("year") Integer year, @Param("month") String month);

    // Ambil semua jadwal berdasarkan tahun
    @Query("SELECT m FROM MaintenanceSchedule m WHERE m.tahun = :year")
    List<MaintenanceSchedule> findByYear(@Param("year") Integer year);
}
