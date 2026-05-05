package com.plr.aduaja.controller;

import com.plr.aduaja.model.Agency;
import com.plr.aduaja.model.Disposition;
import com.plr.aduaja.service.DispositionService;
import com.plr.aduaja.service.AgencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/disposisi")
public class DisposisiApiController {

    @Autowired
    private DispositionService dispositionService;

    @Autowired
    private AgencyService agencyService;

    @GetMapping
    public ResponseEntity<List<Disposition>> getAllDispositions() {
        return ResponseEntity.ok(dispositionService.getAllDispositions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Disposition> getDispositionById(@PathVariable String id) {
        Optional<Disposition> disposition = dispositionService.getDispositionById(id);
        return disposition.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/report/{reportId}")
    public ResponseEntity<Disposition> getDispositionByReportId(@PathVariable String reportId) {
        Optional<Disposition> disposition = dispositionService.getDispositionByReportId(reportId);
        return disposition.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/agency/{agencyId}")
    public ResponseEntity<List<Disposition>> getDispositionsByAgency(@PathVariable String agencyId) {
        return ResponseEntity.ok(dispositionService.getDispositionsByAgency(agencyId));
    }

    @PostMapping
    public ResponseEntity<Disposition> createDisposition(@RequestParam String reportId,
                                                          @RequestParam String dispatchedById,
                                                          @RequestParam String targetAgencyId,
                                                          @RequestParam(required = false) String notes) {
        try {
            Disposition disposition = dispositionService.createDisposition(reportId, dispatchedById, targetAgencyId, notes);
            return ResponseEntity.status(HttpStatus.CREATED).body(disposition);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/agencies")
    public ResponseEntity<List<Agency>> getAllAgencies() {
        return ResponseEntity.ok(agencyService.getActiveAgencies());
    }

    @GetMapping("/agencies/region/{regionId}")
    public ResponseEntity<List<Agency>> getAgenciesByRegion(@PathVariable String regionId) {
        return ResponseEntity.ok(agencyService.getActiveAgenciesByRegion(regionId));
    }
}
