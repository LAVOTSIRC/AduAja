# SCN-10 — Petugas Ajukan Penundaan → Resume → Selesai

**Status Akhir Laporan:** `SELESAI`  
**Aktor:** Petugas → Admin Dinas (approve penundaan) → Petugas (lanjut kerjakan)  
**Estimasi Waktu:** 20 menit  
**Prasyarat:** Ada tugas berstatus BARU atau SEDANG_DIKERJAKAN untuk petugas.

---

## 🗺️ Alur

```
Petugas mulai tugas (SEDANG_DIKERJAKAN)
    → Petugas ajukan penundaan + alasan + estimasi waktu
    → Status penundaan = MENUNGGU (bukan langsung TERTUNDA!)
    → Admin Dinas approve penundaan → Status tugas = TERTUNDA
    → Admin Dinas pause SLA
    → [Kendala selesai]
    → Admin Dinas resume SLA
    → Petugas lanjut kerjakan → selesai
    → Warga konfirmasi → SELESAI
```

---

## LANGKAH DETAIL

### FASE 1 — Setup: Tugas Sedang Dikerjakan

| # | Aksi | Hasil Ekspektasi | ✓/✗ |
|---|------|------------------|-----|
| 1.1 | Ikuti SCN-01 Fase 1–5 hingga petugas START tugas | Status tugas = SEDANG_DIKERJAKAN | `[✓]` |

---

### FASE 2 — Petugas Ajukan Penundaan

🔄 **Petugas**

| # | Aksi | URL | Yang Dicek | Hasil Ekspektasi | ✓/✗ | Catatan                                                              |
|---|------|-----|------------|------------------|-----|----------------------------------------------------------------------|
| 2.1 | Buka detail tugas | `/petugas/task-detail?id=...` | Tombol Penundaan | Tombol "Ajukan Penundaan" tersedia | `[✓]` |                                                                      |
| 2.2 | Klik ajukan penundaan | - | Form | Form alasan + estimasi waktu tampil | `[✓]` |                                                                      |
| 2.3 | Isi alasan penundaan | - | Field alasan | Terisi | `[✓]` |                                                                      |
| 2.4 | Isi estimasi waktu resume | - | Input datetime | Bisa dipilih | `[✓]` |                                                                      |
| 2.5 | **Submit penundaan** | POST `/petugas/task-action` action=postpone | Flash message | "Pengajuan penundaan berhasil dikirim. Menunggu persetujuan admin." | `[✗]` | Tidak ada flash message yang diinginkan                              |
| 2.6 | ⚠️ Cek status tugas BELUM berubah ke TERTUNDA | `/petugas/tasks` | Status tugas | Masih **SEDANG_DIKERJAKAN** (bukan TERTUNDA) | `[✓]` | Tapi di Riwayat status untuk tugas, status tugas langsung ditunda    |
| 2.7 | Cek postponement status di detail tugas | `/petugas/task-detail?id=...` | Status penundaan | "Menunggu persetujuan admin" | `[✗]` | Di section riwayat status untuk tugas, status tugas langsung Ditunda |

---

### FASE 3 — Admin Dinas Approve Penundaan & Pause SLA

🔄 **Admin Dinas**

| # | Aksi | URL | Yang Dicek | Hasil Ekspektasi | ✓/✗ | Catatan                                              |
|---|------|-----|------------|------------------|-----|------------------------------------------------------|
| 3.1 | Cek dashboard / progress | `/admin/dinas/progress` | Notifikasi penundaan | Ada indikasi penundaan menunggu | `[✗]` | Belum ada halaman memantau progress dari admin dinas |
| 3.2 | **Pause SLA** | POST `/admin/dinas/pause-sla` | Flash | "SLA berhasil dijeda" | `[✗]` | Belum ada cara pausa SLA dari admin dinas            |
| 3.3 | Cek SLA status = TERTUNDA | SLA monitoring | Status SLA | TERTUNDA | `[✗]` |                                                      |
| 3.4 | Catat waktu pause SLA | - | Timestamp | CATAT: _______ | `[✗]` |                                                      |

> Note: Approval penundaan secara resmi mungkin dilakukan via admin dashboard — cek apakah ada endpoint approve postponement.

---

### FASE 4 — Lanjut Resume Setelah Kendala Selesai

🔄 **Admin Dinas**

| # | Aksi | URL | Yang Dicek | Hasil Ekspektasi | ✓/✗ | Catatan |
|---|------|-----|------------|------------------|-----|---------|
| 4.1 | **Resume SLA** | POST `/admin/dinas/resume-sla` | Flash | "SLA berhasil dilanjutkan" | `[✗]` | |
| 4.2 | Cek SLA deadline diperpanjang | SLA monitoring | Deadline | Deadline + durasi pause = deadline baru | `[✗]` | |

🔄 **Petugas**

| # | Aksi | URL | Yang Dicek | Hasil Ekspektasi | ✓/✗ | Catatan                                                                        |
|---|------|-----|------------|------------------|-----|--------------------------------------------------------------------------------|
| 4.3 | Lanjut kerjakan tugas | `/petugas/task-execution?id=...` | - | Upload foto & selesaikan | `[✓]` | Petugas bisa melanjutkan pekerjaan yang ditunda oleh dirinya, tanpa izin admin |
| 4.4 | Selesaikan tugas | POST action=complete | Redirect dashboard | Tugas selesai | `[✓]` |                                                                                |

🔄 **Warga**

| # | Aksi | URL | Yang Dicek | Hasil Ekspektasi | ✓/✗ | Catatan                              |
|---|------|-----|------------|------------------|-----|--------------------------------------|
| 4.5 | Cek status | `/warga/report-detail` | Status | MENUNGGU_KONFIRMASI | `[✓]` |                                      |
| 4.6 | Konfirmasi selesai | POST `/warga/confirm-report` | Flash | Laporan dikonfirmasi | `[✓]` | Tidak ada flash laporan dikonfirmasi |
| 4.7 | Status akhir | - | Status | **SELESAI** | `[✓]` |                                      |

---

## ✅ Kriteria LULUS

- [✗] Pengajuan penundaan tidak langsung mengubah status tugas ke TERTUNDA
- [✗] Status penundaan = MENUNGGU sampai admin approve
- [✗] Admin bisa pause dan resume SLA
- [✗] Deadline SLA diperpanjang setelah resume
- [✗] Petugas bisa lanjut mengerjakan tugas setelah resume

**Hasil Akhir:** `[ ] LULUS` / `[✗] GAGAL`  
**Catatan Bug:** belum ada fitur penundaan tugas, approval penundaan, dan pause/resume SLA dari admin dinas. Setelah fitur tersedia, langkah-langkah di atas perlu diuji kembali. serta statug tugas di riwayat tugas langsung berubah ke TERTUNDA setelah pengajuan penundaan, padahal seharusnya menunggu approval admin dinas terlebih dahulu.
