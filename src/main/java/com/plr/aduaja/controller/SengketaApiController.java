package com.plr.aduaja.controller;

import com.plr.aduaja.dto.DisputeDTO;
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
    private DisputeService disputeService;  // ← Interface (ABSTRACTION)

    @GetMapping
    public ResponseEntity<List<DisputeRecord>> getAllDisputes() {
        return ResponseEntity.ok(disputeService.getAllDisputes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisputeRecord> getDisputeById(@PathVariable String id) {
        Optional<DisputeRecord> dispute = disputeService.getDisputeById(id);
        return dispute.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/report/{reportId}")
    public ResponseEntity<List<DisputeRecord>> getDisputeByReportId(@PathVariable String reportId) {
        List<DisputeRecord> disputes = disputeService.getDisputes(reportId);
        return ResponseEntity.ok(disputes);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<DisputeRecord>> getPendingDisputes() {
        // Pending = resolution is null (TUGASKAN_KEMBALI or TUTUP_LAPORAN not yet set)
        return ResponseEntity.ok(disputeService.getDisputes((ResolutionType) null));
    }

    @PostMapping
    public ResponseEntity<DisputeRecord> createDispute(@RequestParam String reportId,
                                                        @RequestParam String filedById,
                                                        @RequestParam String reasonText,
                                                        @RequestParam(required = false) String evidencePhotoUrl) {
        try {
            DisputeDTO dto = new DisputeDTO();
            dto.setReportId(reportId);
            dto.setReason(reasonText);
            dto.setEvidencePhotoUrl(evidencePhotoUrl != null ? evidencePhotoUrl : "");
            DisputeRecord dispute = disputeService.createDispute(dto, filedById);
            return ResponseEntity.status(HttpStatus.CREATED).body(dispute);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<DisputeRecord> resolveDispute(@PathVariable String id,
                                                         @RequestParam ResolutionType resolution,
                                                         @RequestParam(required = false) String resolutionNotes,
                                                         @RequestParam String resolvedById) {
        try {
            return ResponseEntity.ok(disputeService.resolveDispute(
                    id, resolution, resolvedById, resolutionNotes != null ? resolutionNotes : ""));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
