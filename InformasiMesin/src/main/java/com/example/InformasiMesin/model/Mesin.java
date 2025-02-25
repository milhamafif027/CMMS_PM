package com.example.InformasiMesin.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "mesin")
public class Mesin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_no")
    private String entityNo;

    @Column(name = "entity_name")
    private String entityName;

    @Column(name = "brand_type")
    private String brandType;

    @Column(name = "qty_grm")
    private Integer qtyGrm;

    @OneToMany(mappedBy = "mesin", cascade = CascadeType.ALL)
    private List<MaintenanceSchedule> schedules;
}