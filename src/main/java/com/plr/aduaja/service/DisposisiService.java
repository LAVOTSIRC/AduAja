package com.plr.aduaja.service;

import com.plr.aduaja.model.Disposisi;
import com.plr.aduaja.model.Dinas;
import com.plr.aduaja.model.Report;
import com.plr.aduaja.model.User;
import com.plr.aduaja.repository.DisposisiRepository;
import com.plr.aduaja.repository.DinasRepository;
import com.plr.aduaja.repository.ReportRepository;
import com.plr.aduaja.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DisposisiService {

    @Autowired
    private DisposisiRepository disposisiRepository;

    @Autowired
    private DinasRepository dinasRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Disposisi> getAllDisposisi() {
        return disposisiRepository.findAll();
    }

    public List<Disposisi> getDisposisiByStatus(Disposisi.Status status) {
        return disposisiRepository.findByStatus(status);
    }

    public List<Disposisi> getDisposisiByDinas(String dinasId) {
        return disposisiRepository.findByTargetDinasId(dinasId);
    }

    public Optional<Disposisi> getDisposisiById(String id) {
        return disposisiRepository.findById(id);
    }

    public Disposisi createDisposisi(String reportId, String dinasId, String assignedById, String catatan, LocalDateTime deadline) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        Dinas dinas = dinasRepository.findById(dinasId)
                .orElseThrow(() -> new RuntimeException("Dinas not found"));

        User assignedBy = userRepository.findById(assignedById)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Disposisi disposisi = new Disposisi();
        disposisi.setReport(report);
        disposisi.setTargetDinas(dinas);
        disposisi.setAssignedBy(assignedBy);
        disposisi.setCatatan(catatan);
        disposisi.setDeadline(deadline);
        disposisi.setAssignedAt(LocalDateTime.now());
        disposisi.setStatus(Disposisi.Status.MENUNGGU);

        return disposisiRepository.save(disposisi);
    }

    public Disposisi updateStatus(String id, Disposisi.Status status) {
        Disposisi disposisi = disposisiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Disposisi not found"));
        disposisi.setStatus(status);
        return disposisiRepository.save(disposisi);
    }

    public List<Dinas> getAllDinas() {
        return dinasRepository.findAll();
    }

    public List<Dinas> getDinasByCategory(String category) {
        return dinasRepository.findByCategoriesContaining(category);
    }

    public long countByStatus(Disposisi.Status status) {
        return disposisiRepository.countByStatus(status);
    }
}
