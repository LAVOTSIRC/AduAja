package com.plr.aduaja.service;

import com.plr.aduaja.model.MergeCluster;
import com.plr.aduaja.model.Report;
import com.plr.aduaja.model.User;
import com.plr.aduaja.repository.MergeClusterRepository;
import com.plr.aduaja.repository.ReportRepository;
import com.plr.aduaja.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MergeClusterService {

    @Autowired
    private MergeClusterRepository mergeClusterRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    public List<MergeCluster> getAllClusters() {
        return mergeClusterRepository.findAll();
    }

    public List<MergeCluster> getClustersByStatus(MergeCluster.Status status) {
        return mergeClusterRepository.findByStatusOrderByMergedAtDesc(status);
    }

    public Optional<MergeCluster> getClusterById(String id) {
        return mergeClusterRepository.findById(id);
    }

    public MergeCluster createCluster(String parentReportId, List<String> childReportIds, Integer similarityScore) {
        Report parentReport = reportRepository.findById(parentReportId)
                .orElseThrow(() -> new RuntimeException("Parent report not found"));

        MergeCluster cluster = new MergeCluster();
        cluster.setParentReport(parentReport);
        cluster.setSimilarityScore(similarityScore);
        cluster.setStatus(MergeCluster.Status.PENDING);

        List<Report> childReports = new ArrayList<>();
        for (String childId : childReportIds) {
            Report child = reportRepository.findById(childId)
                    .orElseThrow(() -> new RuntimeException("Child report not found: " + childId));
            childReports.add(child);
        }
        cluster.setChildReports(childReports);

        return mergeClusterRepository.save(cluster);
    }

    public MergeCluster mergeCluster(String clusterId, String mergedById) {
        MergeCluster cluster = mergeClusterRepository.findById(clusterId)
                .orElseThrow(() -> new RuntimeException("Cluster not found"));

        User mergedBy = userRepository.findById(mergedById)
                .orElseThrow(() -> new RuntimeException("User not found"));

        cluster.setMergedBy(mergedBy);
        cluster.setMergedAt(LocalDateTime.now());
        cluster.setStatus(MergeCluster.Status.MERGED);

        Report parentReport = cluster.getParentReport();
        parentReport.setSimilarityScore(cluster.getSimilarityScore());
        reportRepository.save(parentReport);

        for (Report child : cluster.getChildReports()) {
            child.setStatus(Report.Status.DITOLAK);
            child.setRejectionReason("Laporan digabungkan ke " + parentReport.getId());
            reportRepository.save(child);
        }

        return mergeClusterRepository.save(cluster);
    }

    public MergeCluster cancelCluster(String clusterId) {
        MergeCluster cluster = mergeClusterRepository.findById(clusterId)
                .orElseThrow(() -> new RuntimeException("Cluster not found"));

        cluster.setStatus(MergeCluster.Status.CANCELLED);
        return mergeClusterRepository.save(cluster);
    }

    public List<List<Report>> detectDuplicateClusters(double similarityThreshold) {
        List<Report> pendingReports = reportRepository.findByStatus(Report.Status.MENUNGGU);
        List<List<Report>> clusters = new ArrayList<>();

        for (int i = 0; i < pendingReports.size(); i++) {
            Report r1 = pendingReports.get(i);
            List<Report> cluster = new ArrayList<>();
            cluster.add(r1);

            for (int j = i + 1; j < pendingReports.size(); j++) {
                Report r2 = pendingReports.get(j);
                int score = calculateSimilarity(r1, r2);
                if (score >= similarityThreshold) {
                    cluster.add(r2);
                }
            }

            if (cluster.size() > 1) {
                clusters.add(cluster);
            }
        }

        return clusters;
    }

    private int calculateSimilarity(Report r1, Report r2) {
        int score = 0;

        if (r1.getCategory() != null && r1.getCategory().equals(r2.getCategory())) {
            score += 30;
        }

        if (r1.getTitle() != null && r2.getTitle() != null) {
            String t1 = r1.getTitle().toLowerCase();
            String t2 = r2.getTitle().toLowerCase();
            if (t1.contains(t2) || t2.contains(t1)) {
                score += 40;
            } else {
                String[] words1 = t1.split("\\s+");
                String[] words2 = t2.split("\\s+");
                for (String w1 : words1) {
                    for (String w2 : words2) {
                        if (w1.length() > 3 && w1.equals(w2)) {
                            score += 10;
                        }
                    }
                }
            }
        }

        if (r1.getLocation() != null && r2.getLocation() != null) {
            String loc1 = r1.getLocation().toLowerCase();
            String loc2 = r2.getLocation().toLowerCase();
            if (loc1.contains(loc2) || loc2.contains(loc1)) {
                score += 20;
            }
        }

        if (r1.getLatitude() != null && r2.getLatitude() != null &&
                r1.getLongitude() != null && r2.getLongitude() != null) {
            try {
                double lat1 = Double.parseDouble(r1.getLatitude());
                double lon1 = Double.parseDouble(r1.getLongitude());
                double lat2 = Double.parseDouble(r2.getLatitude());
                double lon2 = Double.parseDouble(r2.getLongitude());
                double distance = Math.sqrt(Math.pow(lat1 - lat2, 2) + Math.pow(lon1 - lon2, 2));
                if (distance < 0.01) {
                    score += 10;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return Math.min(score, 100);
    }

    public long countByStatus(MergeCluster.Status status) {
        return mergeClusterRepository.countByStatus(status);
    }
}
