package com.plr.aduaja.service;

import java.util.List;
import java.util.Map;

// ============================================================
// ABSTRACTION — Interface kontrak untuk SlaMonitoringService
// ============================================================
public interface SlaMonitoringService {

    Map<String, Object> getSlaStatistics();
    List<Map<String, Object>> getLateItems();
    Map<String, Object> getReportSlaStatus(String reportId);

    // ============ OVERLOADING: nama method SAMA, parameter BERBEDA ============ //
    List<Map<String, Object>> getSlaSummary();               // tanpa parameter
    List<Map<String, Object>> getSlaSummary(String dinasId); // 1 parameter String — OVERLOAD
}
