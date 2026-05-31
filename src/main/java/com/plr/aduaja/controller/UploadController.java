package com.plr.aduaja.controller;

import com.plr.aduaja.service.ImageMigrationService;
import com.plr.aduaja.service.SupabaseStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Autowired
    private SupabaseStorageService supabaseStorageService;

    @Autowired
    private ImageMigrationService imageMigrationService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("jenisGambar") String jenisGambar) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "File tidak boleh kosong"
            ));
        }

        if (jenisGambar == null || jenisGambar.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Parameter jenisGambar wajib diisi"
            ));
        }

        try {
            String url = supabaseStorageService.upload(file, jenisGambar);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "url", url,
                "message", "Upload berhasil"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Gagal upload: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/migrate")
    public ResponseEntity<Map<String, Object>> migrate() {
        try {
            imageMigrationService.migrateAll();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Migrasi gambar ke Supabase selesai!"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Gagal migrasi: " + e.getMessage()
            ));
        }
    }
}
