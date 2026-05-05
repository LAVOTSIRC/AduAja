package com.plr.aduaja.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "merge_clusters")
public class MergeCluster {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_report_id", nullable = false)
    private Report parentReport;

    @ManyToMany
    @JoinTable(
            name = "merge_cluster_reports",
            joinColumns = @JoinColumn(name = "cluster_id"),
            inverseJoinColumns = @JoinColumn(name = "report_id")
    )
    private List<Report> childReports = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merged_by")
    private User mergedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "similarity_score")
    private Integer similarityScore;

    @Column(name = "merged_at")
    private LocalDateTime mergedAt;

    public enum Status {
        PENDING, MERGED, CANCELLED
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Report getParentReport() { return parentReport; }
    public void setParentReport(Report parentReport) { this.parentReport = parentReport; }

    public List<Report> getChildReports() { return childReports; }
    public void setChildReports(List<Report> childReports) { this.childReports = childReports; }

    public User getMergedBy() { return mergedBy; }
    public void setMergedBy(User mergedBy) { this.mergedBy = mergedBy; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Integer getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(Integer similarityScore) { this.similarityScore = similarityScore; }

    public LocalDateTime getMergedAt() { return mergedAt; }
    public void setMergedAt(LocalDateTime mergedAt) { this.mergedAt = mergedAt; }
}
