package com.plr.aduaja.repository;

import com.plr.aduaja.model.TaskPostponement;
import com.plr.aduaja.model.TaskPostponement.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskPostponementRepository extends JpaRepository<TaskPostponement, String> {

    List<TaskPostponement> findByTaskTaskId(String taskId);

    List<TaskPostponement> findByRequestedByUserId(String requestedByUserId);

    List<TaskPostponement> findByApprovalStatus(ApprovalStatus approvalStatus);

    List<TaskPostponement> findByTaskTaskIdOrderByRequestedAtDesc(String taskId);
}
