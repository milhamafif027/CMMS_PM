package com.example.serverapp.repository;

import com.example.serverapp.model.MaintenanceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, Long> {
    // Cari jadwal berdasarkan mesin dan tahun
    List<MaintenanceSchedule> findByMesinIdAndTahun(Long mesinId, Integer tahun);

    // Cari jadwal berdasarkan mesin, tahun, dan bulan (untuk laporan bulanan)
    List<MaintenanceSchedule> findByMesinIdAndTahunAndBulan(Long mesinId, Integer tahun, String bulan);

}

