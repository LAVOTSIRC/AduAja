package com.plr.aduaja.repository;

import com.plr.aduaja.model.Disposisi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisposisiRepository extends JpaRepository<Disposisi, String> {

    List<Disposisi> findByStatus(Disposisi.Status status);

    List<Disposisi> findByTargetDinasId(String dinasId);

    List<Disposisi> findByAssignedById(String assignedById);

    List<Disposisi> findByReportId(String reportId);

    List<Disposisi> findByTargetDinasIdAndStatus(String dinasId, Disposisi.Status status);

    @Query("SELECT d FROM Disposisi d JOIN d.report r WHERE r.category = :category")
    List<Disposisi> findByReportCategory(@Param("category") String category);

    long countByStatus(Disposisi.Status status);

    long countByTargetDinasId(String dinasId);
}
