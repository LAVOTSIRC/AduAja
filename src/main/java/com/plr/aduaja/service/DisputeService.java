package com.plr.aduaja.service;

import com.plr.aduaja.dto.DisputeDTO;
import com.plr.aduaja.model.DisputeRecord;
import com.plr.aduaja.model.DisputeRecord.ResolutionType;

import java.util.List;
import java.util.Optional;

// ============================================================
// ABSTRACTION — Interface kontrak untuk DisputeService
// ============================================================
public interface DisputeService {

    DisputeRecord createDispute(DisputeDTO dto, String disputantId);
    DisputeRecord resolveDispute(String disputeId, ResolutionType resolution, String adminId, String resolutionNotes);
    Optional<DisputeRecord> getDisputeById(String disputeId);

    // ============ OVERLOADING: nama method SAMA, parameter BERBEDA ============ //
    List<DisputeRecord> getDisputes(String reportId);             // 1 parameter String (reportId)
    List<DisputeRecord> getDisputes(ResolutionType resolution);   // 1 parameter ResolutionType — OVERLOAD

    List<DisputeRecord> getAllDisputes();

    List<DisputeRecord> getPendingDisputes();
}
