package com.plr.aduaja.service;

import com.plr.aduaja.model.ValidationDecision;
import com.plr.aduaja.model.ValidationDecision.Decision;

import java.util.List;

// ============================================================
// ABSTRACTION — Interface kontrak untuk ValidationDecisionService
// ============================================================
public interface ValidationDecisionService {

    ValidationDecision createDecision(String reportId, String adminId, Decision decision, String reason);

    // ============ OVERLOADING: nama method SAMA, parameter BERBEDA ============ //
    List<ValidationDecision> getDecisions();                         // tanpa parameter
    List<ValidationDecision> getDecisions(String reportOrAdminId);  // 1 parameter String — OVERLOAD
}
