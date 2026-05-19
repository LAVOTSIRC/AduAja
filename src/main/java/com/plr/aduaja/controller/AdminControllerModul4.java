package com.plr.aduaja.controller;

import com.plr.aduaja.dto.DisputeDTO;
import com.plr.aduaja.dto.MergeDTO;
import com.plr.aduaja.model.DisputeRecord;
import com.plr.aduaja.model.DisputeRecord.ResolutionType;
import com.plr.aduaja.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ============================================================
// ABSTRACTION — Controller hanya inject Interface, tidak tahu Impl
// Semua field bertipe Interface, bukan Impl class
// ============================================================
@Controller
@RequestMapping("/admin")
public class AdminControllerModul4 {

    @Autowired
    private DisputeService disputeService;                    // ← Interface (ABSTRACTION)

    @Autowired
    private MergeRecordService mergeRecordService;            // ← Interface (ABSTRACTION)

    @Autowired
    private SlaRecordService slaRecordService;                // ← Interface (ABSTRACTION)

    @Autowired
    private SlaMonitoringService slaMonitoringService;         // ← Interface (ABSTRACTION)

    // ============ SENGKETA ============ //

    @GetMapping("/sengketa")
    public String sengketaPanel(Model model) {
        List<DisputeRecord> disputes = disputeService.getAllDisputes();
        model.addAttribute("disputes", disputes);
        model.addAttribute("disputeDTO", new DisputeDTO());
        return "admin/sengketa-panel";
    }

    @PostMapping("/sengketa")
    public String prosesSengketa(@RequestParam String disputeId,
                                  @RequestParam String action,
                                  @RequestParam(required = false, defaultValue = "system") String adminId,
                                  @RequestParam(required = false) String resolutionNotes) {
        ResolutionType resolution = "terima".equals(action)
                ? ResolutionType.TUGASKAN_KEMBALI
                : ResolutionType.TUTUP_LAPORAN;

        disputeService.resolveDispute(disputeId, resolution, adminId,
                resolutionNotes != null ? resolutionNotes : "");
        return "redirect:/admin/sengketa";
    }

    // ============ MERGE TICKET ============ //

    @GetMapping("/merge")
    public String mergePanel(Model model) {
        model.addAttribute("merges", mergeRecordService.getMerges());
        model.addAttribute("mergeDTO", new MergeDTO());
        return "admin/merge-ticket-panel";
    }

    @PostMapping("/merge")
    public String prosesMerge(@ModelAttribute MergeDTO mergeDTO,
                               @RequestParam(required = false, defaultValue = "system") String userId) {
        mergeRecordService.createMerge(mergeDTO, userId);
        return "redirect:/admin/merge";
    }

    // ============ SLA MONITORING ============ //

    @GetMapping("/sla")
    public String slaPanel(Model model) {
        model.addAttribute("slaStats", slaMonitoringService.getSlaStatistics());
        model.addAttribute("lateItems", slaMonitoringService.getLateItems());
        model.addAttribute("allSla", slaRecordService.getAllRecords());
        return "admin/dashboard";
    }
}
