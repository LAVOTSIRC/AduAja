package com.plr.aduaja.controller;

import com.plr.aduaja.model.DisputeRecord;
import com.plr.aduaja.model.DisputeRecord.ResolutionType;
import com.plr.aduaja.service.DisputeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sengketa")
public class SengketaApiController {

    @Autowired
    private DisputeService disputeService;

    @GetMapping
    public ResponseEntity<List<DisputeRecord>> getAllDisputes() {
        return ResponseEntity.ok(disputeService.getAllDisputes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisputeRecord> getDisputeById(@PathVariable String id) {
        return disputeService.getAllDisputes().stream()
                .filter(d -> d.getDisputeId().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/report/{reportId}")
    public ResponseEntity<DisputeRecord> getDisputeByReportId(@PathVariable String reportId) {
        Optional<DisputeRecord> dispute = disputeService.getDisputeByReportId(reportId);
        return dispute.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<DisputeRecord>> getPendingDisputes() {
        return ResponseEntity.ok(disputeService.getPendingDisputes());
    }

    @PostMapping
    public ResponseEntity<DisputeRecord> createDispute(@RequestParam String reportId,
                                                        @RequestParam String filedById,
                                                        @RequestParam String reasonText,
                                                        @RequestParam String evidencePhotoUrl) {
        try {
            DisputeRecord dispute = disputeService.createDispute(reportId, filedById, reasonText, evidencePhotoUrl);
            return ResponseEntity.status(HttpStatus.CREATED).body(dispute);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<DisputeRecord> resolveDispute(@PathVariable String id,
                                                         @RequestParam ResolutionType resolution,
                                                         @RequestParam String resolutionNotes,
                                                         @RequestParam String resolvedById) {
        try {
            return ResponseEntity.ok(disputeService.resolveDispute(id, resolvedById, resolution, resolutionNotes));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
