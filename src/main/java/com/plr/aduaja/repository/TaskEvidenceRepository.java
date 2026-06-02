package com.plr.aduaja.repository;

import com.plr.aduaja.model.TaskEvidence;
import com.plr.aduaja.model.TaskEvidence.EvidenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskEvidenceRepository extends JpaRepository<TaskEvidence, String> {

    List<TaskEvidence> findByTaskTaskId(String taskId);

    List<TaskEvidence> findByTaskTaskIdAndEvidenceType(String taskId, EvidenceType evidenceType);

    List<TaskEvidence> findByTaskReportReportId(String reportId);
}
