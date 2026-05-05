package com.plr.aduaja.repository;

import com.plr.aduaja.model.Laporan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LaporanRepository extends JpaRepository<Laporan, Long> {
    // Kosongkan saja.
}