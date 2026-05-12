package com.plr.aduaja.repository;

import com.plr.aduaja.model.ReportCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportCategoryRepository extends JpaRepository<ReportCategory, String> {

    Optional<ReportCategory> findByCategoryName(String categoryName);

    boolean existsByCategoryName(String categoryName);

    // Untuk dropdown form buat laporan (hanya kategori aktif)
    List<ReportCategory> findByIsActiveTrue();

    // Alias untuk backward compatibility
    default Optional<ReportCategory> findByName(String name) {
        return findByCategoryName(name);
    }

    default boolean existsByName(String name) {
        return existsByCategoryName(name);
    }
}
