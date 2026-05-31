package com.plr.aduaja.service;

import com.plr.aduaja.dto.MergeDTO;
import com.plr.aduaja.model.MergeRecord;
import com.plr.aduaja.model.Report;
import com.plr.aduaja.model.User;
import com.plr.aduaja.repository.MergeRecordRepository;
import com.plr.aduaja.repository.ReportRepository;
import com.plr.aduaja.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// ============================================================
// POLYMORPHISM — @Override (Run-time Polymorphism)
// ============================================================
@Service
public class MergeRecordServiceImpl implements MergeRecordService {

    @Autowired
    private MergeRecordRepository mergeRecordRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Override  // ← POLYMORPHISM: Override dari interface
    @Transactional
    public MergeRecord createMerge(MergeDTO dto, String userId) {
        Report parent = reportRepository.findById(dto.getPrimaryReportId())
                .orElseThrow(() -> new RuntimeException("Primary report tidak ditemukan: " + dto.getPrimaryReportId()));
        Report child = reportRepository.findById(dto.getMergedReportId())
                .orElseThrow(() -> new RuntimeException("Merged report tidak ditemukan: " + dto.getMergedReportId()));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan: " + userId));

        MergeRecord merge = new MergeRecord();
        merge.setParentReport(parent);
        merge.setChildReport(child);
        merge.setMergedBy(user);
        merge.setMergeReason(dto.getReason());
        merge.setIsActive(true);
        merge.setMergedAt(LocalDateTime.now());

        child.setParentReport(parent);
        reportRepository.save(child);

        return mergeRecordRepository.save(merge);
    }

    @Override  // ← POLYMORPHISM: Override dari interface (OVERLOAD — tanpa parameter)
    public List<MergeRecord> getMerges() {
        return mergeRecordRepository.findAll();
    }

    @Override  // ← POLYMORPHISM: Override dari interface (OVERLOAD — 1 parameter)
    public List<MergeRecord> getMerges(String reportId) {
        List<MergeRecord> primaryMerges = mergeRecordRepository.findByParentReportReportId(reportId);
        List<MergeRecord> childMerges = mergeRecordRepository.findByChildReportReportId(reportId);
        List<MergeRecord> all = new ArrayList<>(primaryMerges);
        all.addAll(childMerges);
        return all;
    }

    @Override  // ← POLYMORPHISM: Override dari interface
    @Transactional
    public void cancelMerge(String mergeId) {
        MergeRecord record = mergeRecordRepository.findById(mergeId)
                .orElseThrow(() -> new RuntimeException("Merge record tidak ditemukan: " + mergeId));
        record.setIsActive(false);

        Report child = record.getChildReport();
        child.setParentReport(null);
        reportRepository.save(child);

        mergeRecordRepository.save(record);
    }
}
