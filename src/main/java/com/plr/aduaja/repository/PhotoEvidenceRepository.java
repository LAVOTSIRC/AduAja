package com.plr.aduaja.repository;

import com.plr.aduaja.model.PhotoEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoEvidenceRepository extends JpaRepository<PhotoEvidence, String> {

    List<PhotoEvidence> findByReportId(String reportId);

    List<PhotoEvidence> findByTicketId(String ticketId);

    List<PhotoEvidence> findByPhotoType(String photoType);
}
