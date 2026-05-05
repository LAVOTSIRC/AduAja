package com.plr.aduaja.repository;

import com.plr.aduaja.model.MergeCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MergeClusterRepository extends JpaRepository<MergeCluster, String> {

    List<MergeCluster> findByStatus(MergeCluster.Status status);

    List<MergeCluster> findByMergedById(String mergedById);

    List<MergeCluster> findByParentReportId(String parentReportId);

    List<MergeCluster> findByStatusOrderByMergedAtDesc(MergeCluster.Status status);

    long countByStatus(MergeCluster.Status status);
}
