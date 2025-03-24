package com.example.serverapp.repository;

import com.example.serverapp.model.Mesin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MesinRepository extends JpaRepository<Mesin, Long> {
    Optional<Mesin> findByEntityNo(String entityNo);
}