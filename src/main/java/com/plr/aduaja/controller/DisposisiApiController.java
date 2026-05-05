package com.plr.aduaja.controller;

import com.plr.aduaja.model.Disposisi;
import com.plr.aduaja.model.Dinas;
import com.plr.aduaja.service.DisposisiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/disposisi")
public class DisposisiApiController {

    @Autowired
    private DisposisiService disposisiService;

    @GetMapping
    public ResponseEntity<List<Disposisi>> getAllDisposisi() {
        return ResponseEntity.ok(disposisiService.getAllDisposisi());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Disposisi> getDisposisiById(@PathVariable String id) {
        Optional<Disposisi> disposisi = disposisiService.getDisposisiById(id);
        return disposisi.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Disposisi>> getDisposisiByStatus(@PathVariable Disposisi.Status status) {
        return ResponseEntity.ok(disposisiService.getDisposisiByStatus(status));
    }

    @GetMapping("/dinas/{dinasId}")
    public ResponseEntity<List<Disposisi>> getDisposisiByDinas(@PathVariable String dinasId) {
        return ResponseEntity.ok(disposisiService.getDisposisiByDinas(dinasId));
    }

    @PostMapping
    public ResponseEntity<Disposisi> createDisposisi(@RequestParam String reportId,
                                                      @RequestParam String dinasId,
                                                      @RequestParam String assignedById,
                                                      @RequestParam(required = false) String catatan,
                                                      @RequestParam(required = false) LocalDateTime deadline) {
        try {
            Disposisi disposisi = disposisiService.createDisposisi(reportId, dinasId, assignedById, catatan, deadline);
            return ResponseEntity.status(HttpStatus.CREATED).body(disposisi);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Disposisi> updateStatus(@PathVariable String id,
                                                   @RequestParam Disposisi.Status status) {
        try {
            return ResponseEntity.ok(disposisiService.updateStatus(id, status));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/dinas")
    public ResponseEntity<List<Dinas>> getAllDinas() {
        return ResponseEntity.ok(disposisiService.getAllDinas());
    }

    @GetMapping("/dinas/search")
    public ResponseEntity<List<Dinas>> getDinasByCategory(@RequestParam String category) {
        return ResponseEntity.ok(disposisiService.getDinasByCategory(category));
    }
}
