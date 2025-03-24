package com.example.serverapp.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "mesin")
public class Mesin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entityNo;
    private String entityName;
    private String brandType;
    private Integer qtyGrm;
}