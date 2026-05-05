package com.plr.aduaja.controller;

import com.plr.aduaja.model.MergeCluster;
import com.plr.aduaja.model.Report;
import com.plr.aduaja.service.MergeClusterService;
import com.plr.aduaja.service.SlaMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AdminApiController {

    @Autowired
    private MergeClusterService mergeClusterService;

    @Autowired
    private SlaMonitoringService slaMonitoringService;

    @GetMapping("/merge-clusters")
    public ResponseEntity<List<MergeCluster>> getAllClusters() {
        return ResponseEntity.ok(mergeClusterService.getAllClusters());
    }

    @GetMapping("/merge-clusters/status/{status}")
    public ResponseEntity<List<MergeCluster>> getClustersByStatus(@PathVariable MergeCluster.Status status) {
        return ResponseEntity.ok(mergeClusterService.getClustersByStatus(status));
    }

    @GetMapping("/merge-clusters/{id}")
    public ResponseEntity<MergeCluster> getClusterById(@PathVariable String id) {
        return mergeClusterService.getClusterById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/merge-clusters")
    public ResponseEntity<MergeCluster> createCluster(@RequestParam String parentReportId,
                                                       @RequestParam List<String> childReportIds,
                                                       @RequestParam(required = false) Integer similarityScore) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    mergeClusterService.createCluster(parentReportId, childReportIds, similarityScore));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/merge-clusters/{id}/merge")
    public ResponseEntity<MergeCluster> mergeCluster(@PathVariable String id,
                                                      @RequestParam String mergedById) {
        try {
            return ResponseEntity.ok(mergeClusterService.mergeCluster(id, mergedById));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/merge-clusters/{id}/cancel")
    public ResponseEntity<MergeCluster> cancelCluster(@PathVariable String id) {
        try {
            return ResponseEntity.ok(mergeClusterService.cancelCluster(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/merge-clusters/detect")
    public ResponseEntity<List<List<Report>>> detectDuplicates(@RequestParam(required = false, defaultValue = "60") double threshold) {
        return ResponseEntity.ok(mergeClusterService.detectDuplicateClusters(threshold));
    }

    @GetMapping("/sla/statistics")
    public ResponseEntity<Map<String, Object>> getSlaStatistics() {
        return ResponseEntity.ok(slaMonitoringService.getSlaStatistics());
    }

    @GetMapping("/sla/late-items")
    public ResponseEntity<List<Map<String, Object>>> getLateItems() {
        return ResponseEntity.ok(slaMonitoringService.getLateItems());
    }
}
