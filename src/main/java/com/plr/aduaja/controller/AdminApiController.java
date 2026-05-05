package com.plr.aduaja.controller;

import com.plr.aduaja.model.MergeRecord;
import com.plr.aduaja.model.Report;
import com.plr.aduaja.service.MergeRecordService;
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
    private MergeRecordService mergeRecordService;

    @Autowired
    private SlaMonitoringService slaMonitoringService;

    @GetMapping("/merge-records")
    public ResponseEntity<List<MergeRecord>> getAllMergeRecords() {
        return ResponseEntity.ok(mergeRecordService.getAllMergeRecords());
    }

    @GetMapping("/merge-records/active")
    public ResponseEntity<List<MergeRecord>> getActiveMergeRecords() {
        return ResponseEntity.ok(mergeRecordService.getActiveMergeRecords());
    }

    @GetMapping("/merge-records/{id}")
    public ResponseEntity<MergeRecord> getMergeRecordById(@PathVariable String id) {
        return mergeRecordService.getAllMergeRecords().stream()
                .filter(m -> m.getMergeId().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/merge-records")
    public ResponseEntity<MergeRecord> createMergeRecord(@RequestParam String parentReportId,
                                                          @RequestParam String childReportId,
                                                          @RequestParam String mergedById,
                                                          @RequestParam(required = false) String mergeReason) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    mergeRecordService.createMerge(parentReportId, childReportId, mergedById, mergeReason));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/merge-records/{id}/undo")
    public ResponseEntity<MergeRecord> undoMerge(@PathVariable String id) {
        try {
            return ResponseEntity.ok(mergeRecordService.undoMerge(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
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
