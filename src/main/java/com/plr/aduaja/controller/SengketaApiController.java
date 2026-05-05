package com.plr.aduaja.controller;

import com.plr.aduaja.model.Sengketa;
import com.plr.aduaja.service.SengketaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sengketa")
public class SengketaApiController {

    @Autowired
    private SengketaService sengketaService;

    @GetMapping
    public ResponseEntity<List<Sengketa>> getAllSengketa() {
        return ResponseEntity.ok(sengketaService.getAllSengketa());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sengketa> getSengketaById(@PathVariable String id) {
        Optional<Sengketa> sengketa = sengketaService.getSengketaById(id);
        return sengketa.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{number}")
    public ResponseEntity<Sengketa> getSengketaByNumber(@PathVariable String number) {
        Optional<Sengketa> sengketa = sengketaService.getSengketaByNumber(number);
        return sengketa.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Sengketa>> getSengketaByStatus(@PathVariable Sengketa.Status status) {
        return ResponseEntity.ok(sengketaService.getSengketaByStatus(status));
    }

    @PostMapping
    public ResponseEntity<Sengketa> createSengketa(@RequestParam String ticketId,
                                                    @RequestParam String reportId,
                                                    @RequestParam String filedById,
                                                    @RequestParam String alasan,
                                                    @RequestParam(required = false) Sengketa.Priority prioritas) {
        try {
            Sengketa sengketa = sengketaService.createSengketa(ticketId, reportId, filedById, alasan,
                    prioritas != null ? prioritas : Sengketa.Priority.SEDANG);
            return ResponseEntity.status(HttpStatus.CREATED).body(sengketa);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<Sengketa> resolveSengketa(@PathVariable String id,
                                                     @RequestParam String keputusan,
                                                     @RequestParam String catatan,
                                                     @RequestParam String resolvedById) {
        try {
            return ResponseEntity.ok(sengketaService.resolveSengketa(id, keputusan, catatan, resolvedById));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
