package com.plr.aduaja.service;

import com.plr.aduaja.model.Disposition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DispositionService {

    List<Disposition> getAllDispositions();

    Optional<Disposition> getDispositionById(String id);

    Optional<Disposition> getDispositionByReportId(String reportId);

    // OVERLOADING: getDispositions() — Compile-time Polymorphism (nama sama, parameter beda)
    List<Disposition> getDispositions();  // OVERLOAD 1: semua disposisi

    List<Disposition> getDispositions(String agencyId);  // OVERLOAD 2: filter by agency

    List<Disposition> getDispositions(LocalDateTime from, LocalDateTime to);  // OVERLOAD 3: filter by date range

    List<Disposition> getDispositions(String agencyId, LocalDateTime from, LocalDateTime to);  // OVERLOAD 4

    List<Disposition> getDispositionsByAgency(String agencyId);

    List<Disposition> getDispositionsByDispatcher(String dispatchedByUserId);

    Disposition createDisposition(String reportId, String dispatchedById, String targetAgencyId, String notes);
}
