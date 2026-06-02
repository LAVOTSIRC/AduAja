package com.plr.aduaja.service;

import com.plr.aduaja.model.ConfirmationRequest;
import com.plr.aduaja.model.ConfirmationRequest.ResponseType;

import java.util.List;
import java.util.Optional;

// ============================================================
// ABSTRACTION — Interface kontrak untuk ConfirmationService
// ============================================================
public interface ConfirmationService {

    ConfirmationRequest createConfirmation(String reportId, String wargaId, int deadlineHours);
    ConfirmationRequest respond(String reportId, ResponseType response);
    Optional<ConfirmationRequest> getByReportId(String reportId);

    // ============ OVERLOADING: nama method SAMA, parameter BERBEDA ============ //
    List<ConfirmationRequest> getRequests(ResponseType responseType);   // 1 parameter ResponseType
    List<ConfirmationRequest> getRequests(String reportId);             // 1 parameter String — OVERLOAD

    List<ConfirmationRequest> getAllRequests();
    void processTimeouts();
}
