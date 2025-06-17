package com.example.serverapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OptimisticLocking;
import org.hibernate.annotations.OptimisticLockType;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "maintenance_schedule")
@OptimisticLocking(type = OptimisticLockType.VERSION)
public class MaintenanceSchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mesin_id", nullable = false)
    private Mesin mesin;

    @Column(nullable = false, length = 20)
    private String bulan;

    @Column(nullable = false)
    private Integer tanggal;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(nullable = false)
    private Integer tahun;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 100)
    private String technician;
    
    @Column(name = "is_rescheduled", nullable = false)
    private Boolean isRescheduled = false;
    
    @Column(name = "reschedule_reason", length = 500)
    private String rescheduleReason;
    
    @Version
    @Column(nullable = false)
    private Integer version;

    // Pre-persist and pre-update hooks untuk validasi
    @PrePersist
    @PreUpdate
    private void validate() {
        if (tanggal != null && bulan != null && tahun != null) {
            int maxDays = getMaxDaysInMonth();
            if (tanggal < 1 || tanggal > maxDays) {
                throw new IllegalStateException("Invalid date for the given month and year");
            }
        }
    }

    private int getMaxDaysInMonth() {
        return switch (bulan) {
            case "Februari" -> (tahun % 4 == 0 && (tahun % 100 != 0 || tahun % 400 == 0)) ? 29 : 28;
            case "April", "Juni", "September", "November" -> 30;
            default -> 31;
        };
    }
}