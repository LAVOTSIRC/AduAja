package com.plr.aduaja.repository;

import com.plr.aduaja.model.ReportCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportCategoryRepository extends JpaRepository<ReportCategory, String> {

    Optional<ReportCategory> findByCategoryName(String categoryName);

    boolean existsByCategoryName(String categoryName);
}
