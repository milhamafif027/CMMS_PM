package com.example.InformasiMesin.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "maintenance_schedule")
public class MaintenanceSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mesin_id")
    private Mesin mesin;

    private String bulan;
    private Integer tanggal;
    private String status;
    private String action;
    private Integer tahun;
    private String description;
    private String technician;
    private Boolean isRescheduled;
    private String rescheduleReason;
}
