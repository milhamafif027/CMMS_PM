package com.example.InformasiMesin.repository;

import com.example.InformasiMesin.model.MaintenanceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MaintenanceRepository extends JpaRepository<MaintenanceSchedule, Long> {
    List<MaintenanceSchedule> findByMesinIdAndTahun(Long mesinId, Integer tahun);
}