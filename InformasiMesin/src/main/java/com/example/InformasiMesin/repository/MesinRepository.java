package com.example.InformasiMesin.repository;

import com.example.InformasiMesin.model.Mesin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MesinRepository extends JpaRepository<Mesin, Long> {
}