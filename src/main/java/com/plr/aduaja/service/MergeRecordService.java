package com.plr.aduaja.service;

import com.plr.aduaja.model.*;
import com.plr.aduaja.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MergeRecordService {

    @Autowired
    private MergeRecordRepository mergeRecordRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    public List<MergeRecord> getAllMergeRecords() {
        return mergeRecordRepository.findAll();
    }

    public List<MergeRecord> getActiveMergeRecords() {
        return mergeRecordRepository.findByIsActiveTrue();
    }

    public List<MergeRecord> getMergesByParentReport(String parentReportId) {
        return mergeRecordRepository.findByParentReportReportId(parentReportId);
    }

    public MergeRecord createMerge(String parentReportId, String childReportId, String mergedById, String mergeReason) {
        Report parent = reportRepository.findById(parentReportId)
                .orElseThrow(() -> new RuntimeException("Parent report not found"));
        Report child = reportRepository.findById(childReportId)
                .orElseThrow(() -> new RuntimeException("Child report not found"));
        User mergedBy = userRepository.findById(mergedById)
                .orElseThrow(() -> new RuntimeException("User not found"));

        MergeRecord record = new MergeRecord();
        record.setParentReport(parent);
        record.setChildReport(child);
        record.setMergedBy(mergedBy);
        record.setMergeReason(mergeReason);
        record.setIsActive(true);
        record.setMergedAt(LocalDateTime.now());

        child.setParentReport(parent);
        child.setUpdatedAt(LocalDateTime.now());
        reportRepository.save(child);

        return mergeRecordRepository.save(record);
    }

    public MergeRecord undoMerge(String mergeId) {
        MergeRecord record = mergeRecordRepository.findById(mergeId)
                .orElseThrow(() -> new RuntimeException("Merge record not found"));
        record.setIsActive(false);

        Report child = record.getChildReport();
        child.setParentReport(null);
        reportRepository.save(child);

        return mergeRecordRepository.save(record);
    }
}
