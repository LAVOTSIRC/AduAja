package com.plr.aduaja.service;

import com.plr.aduaja.dto.MergeDTO;
import com.plr.aduaja.model.MergeRecord;

import java.util.List;

// ============================================================
// ABSTRACTION — Interface kontrak untuk MergeRecordService
// ============================================================
public interface MergeRecordService {

    MergeRecord createMerge(MergeDTO dto, String userId);

    // ============ OVERLOADING: nama method SAMA, parameter BERBEDA ============ //
    List<MergeRecord> getMerges();                   // tanpa parameter
    List<MergeRecord> getMerges(String reportId);    // 1 parameter String — OVERLOAD

    void cancelMerge(String mergeId);
}
