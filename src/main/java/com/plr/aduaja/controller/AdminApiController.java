package com.plr.aduaja.controller;

import com.plr.aduaja.dto.MergeDTO;
import com.plr.aduaja.model.MergeRecord;
import com.plr.aduaja.service.MergeRecordService;
import com.plr.aduaja.service.SlaMonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class AdminApiController {

    @Autowired
    private MergeRecordService mergeRecordService;  // ← Interface (ABSTRACTION)

    @Autowired
    private SlaMonitoringService slaMonitoringService;  // ← Interface (ABSTRACTION)

    @GetMapping("/merge-records")
    public ResponseEntity<List<MergeRecord>> getAllMergeRecords() {
        // getMerges() tanpa param = getAll (overloaded method)
        return ResponseEntity.ok(mergeRecordService.getMerges());
    }

    @GetMapping("/merge-records/active")
    public ResponseEntity<List<MergeRecord>> getActiveMergeRecords() {
        // Filter active merges dari getAll
        List<MergeRecord> all = mergeRecordService.getMerges();
        List<MergeRecord> active = all.stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsActive()))
                .toList();
        return ResponseEntity.ok(active);
    }

    @GetMapping("/merge-records/{id}")
    public ResponseEntity<MergeRecord> getMergeRecordById(@PathVariable String id) {
        return mergeRecordService.getMerges().stream()
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
            MergeDTO dto = new MergeDTO();
            dto.setPrimaryReportId(parentReportId);
            dto.setMergedReportId(childReportId);
            dto.setReason(mergeReason);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    mergeRecordService.createMerge(dto, mergedById));
        } catch (Exception e) {
            log.error("Gagal create merge record: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/merge-records/{id}/undo")
    public ResponseEntity<String> undoMerge(@PathVariable String id) {
        try {
            mergeRecordService.cancelMerge(id);
            return ResponseEntity.ok("Merge dibatalkan");
        } catch (Exception e) {
            log.error("Gagal undo merge {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
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
