# SCN-08 — Konfirmasi Timeout: Laporan Ditutup Otomatis

**Status Akhir Laporan:** `DITUTUP`  
**Aktor:** Warga (pasif — tidak merespons) + Sistem (scheduler)  
**Estimasi Waktu:** 15 menit (+ simulasi DB)  
**Prasyarat:** Ada laporan yang statusnya MENUNGGU_KONFIRMASI. ConfirmationRequest sudah terbuat.

---

## 🗺️ Alur

```
Petugas selesaikan tugas
    → Laporan otomatis = MENUNGGU_KONFIRMASI
    → ConfirmationRequest dibuat (deadline = sekarang + 72 jam)
    → Warga TIDAK merespons sampai deadline lewat
    → Scheduler: processTimeouts() berjalan
    → ConfirmationRequest.response = TIMEOUT, isLocked = true
    → Status laporan = DITUTUP
    → Notifikasi "Laporan Ditutup Otomatis" dikirim ke warga
```

---

## LANGKAH DETAIL

### FASE 1 — Setup: Bawa Laporan ke Status MENUNGGU_KONFIRMASI

| # | Aksi                                                                                               | Hasil Ekspektasi | ✓/✗ |
|---|----------------------------------------------------------------------------------------------------|------------------|-----|
| 1.1 | Ikuti SCN-01 Fase 1–5 lengkap (sampai petugas selesaikan tugas)                                    | Status laporan = **MENUNGGU_KONFIRMASI** | `[✓]` |
| 1.2 | Cek ConfirmationRequest terbuat, DB: `SELECT * FROM confirmation_requests WHERE report_id = '...'` | Record ada, `response IS NULL`, `deadline_at = now + 72 jam` | `[✓]` |

---

### FASE 2 — Verifikasi Sebelum Timeout

🔄 **Warga**

| # | Aksi | URL | Yang Dicek | Hasil Ekspektasi | ✓/✗ | Catatan |
|---|------|-----|------------|------------------|-----|---------|
| 2.1 | Buka detail laporan | `/warga/report-detail?id=...` | Status | "Menunggu Konfirmasi" | `[✓]` | |
| 2.2 | Cek deadline konfirmasi tampil | - | 📋 Deadline | Countdown/tanggal deadline 72 jam tampil | `[✓]` | |
| 2.3 | Cek tombol yang tersedia | - | 📸 Tombol | "Konfirmasi Selesai" + "Ajukan Sengketa" tersedia | `[✓]` | |
| 2.4 | **Jangan klik apapun** — simulasikan warga tidak merespons | - | - | - | `[✓]` | |

---

### FASE 3 — Simulasi Timeout (Pilih Salah Satu Cara)

**🔧 Cara A: Manipulasi Database (Direkomendasikan untuk Testing)**

| # | Query SQL | Tujuan | ✓/✗ |
|---|-----------|--------|-----|
| 3A.1 | `UPDATE confirmation_requests SET deadline_at = NOW() - INTERVAL '1 hour' WHERE response IS NULL AND report_id = '[ID_LAPORAN]'` | Buat deadline sudah lewat 1 jam | `[✓]` |
| 3A.2 | Trigger scheduler: restart aplikasi atau panggil endpoint manual jika ada | `processTimeouts()` dijalankan | `[✗]` |

**🕐 Cara B: Tunggu Scheduler Alami (Jika Ada `@Scheduled`)**

| # | Aksi | Yang Dicek | Hasil Ekspektasi | ✓/✗ |
|---|------|------------|------------------|-----|
| 3B.1 | Cari `@Scheduled` di kode — cek apakah ada scheduled task untuk `processTimeouts()` | Kelas Service/Scheduler | Ada `@Scheduled` yang memanggil `confirmationService.processTimeouts()` | `[✗]` |
| 3B.2 | Tunggu scheduler berjalan (setelah 72 jam atau sesuai interval) | - | Status berubah otomatis | `[✗]` |

---

### FASE 4 — Verifikasi Setelah Timeout

🔄 **Warga**

| # | Aksi | URL | Yang Dicek | Hasil Ekspektasi | ✓/✗ | Catatan                                                                  |
|---|------|-----|------------|------------------|-----|--------------------------------------------------------------------------|
| 4.1 | Cek notifikasi | `/warga/notifications` | 📋 Notif baru | **"Laporan Ditutup Otomatis"** muncul | `[✗]` |                                                                          |
| 4.2 | Baca isi notifikasi | - | 📋 Pesan | Berisi penjelasan "batas waktu konfirmasi 3x24 jam telah habis" | `[✗]` |                                                                          |
| 4.3 | Buka detail laporan | `/warga/report-detail?id=...` | 📋 Status | Status = **"Ditutup"** | `[✗]` |                                                                          |
| 4.4 | Cek tombol aksi | - | 📸 Tombol | Tidak ada tombol apapun (status final) | `[✗]` | tidak bisa di test karena belum ada cara laporan tertutup akibat timeout |
| 4.5 | ⚠️ Coba ajukan sengketa setelah DITUTUP | POST `/warga/dispute-report` | Error/redirect | Tidak bisa, laporan sudah ditutup | `[✗]` |                                                                          |
| 4.6 | Cek riwayat laporan | `/warga/report-history` | Filter status | Laporan tampil dengan label "Ditutup" | `[✗]` |                                                                          |

**Verifikasi Database:**

| # | Query SQL | Hasil Ekspektasi | ✓/✗ |
|---|-----------|------------------|-----|
| 4.7 | `SELECT response, responded_at, is_locked FROM confirmation_requests WHERE report_id = '[ID]'` | `response = 'TIMEOUT'`, `is_locked = true`, `responded_at` terisi | `[✗]` |
| 4.8 | `SELECT status FROM reports WHERE report_id = '[ID]'` | `status = 'DITUTUP'` | `[✗]` |

---

### FASE 5 — Bandingkan dengan Konfirmasi Manual (Kontrol)

> Pastikan timeout berbeda dengan konfirmasi manual (TERIMA menghasilkan SELESAI, bukan DITUTUP)

| # | Aksi | Hasil Ekspektasi | ✓/✗ |
|---|------|------------------|-----|
| 5.1 | Buat laporan baru, jalankan alur sampai MENUNGGU_KONFIRMASI | Status = MENUNGGU_KONFIRMASI | `[✓]` |
| 5.2 | Warga klik **"Konfirmasi Selesai"** (manual, sebelum timeout) | Status = **SELESAI** (bukan DITUTUP) | `[✓]` |
| 5.3 | Bandingkan: TIMEOUT → DITUTUP vs TERIMA → SELESAI | Dua status akhir berbeda | `[✗]` |

---

## ✅ Kriteria LULUS

- [✓] ConfirmationRequest terbuat dengan deadline 72 jam saat petugas selesaikan tugas
- [✗] Setelah deadline lewat + scheduler jalan → status laporan = DITUTUP
- [✗] Warga mendapat notifikasi "Laporan Ditutup Otomatis"
- [✗] ConfirmationRequest.response = TIMEOUT dan isLocked = true di DB
- [✗] Warga tidak bisa melakukan aksi apapun setelah DITUTUP
- [✗] Berbeda dari konfirmasi manual (TERIMA → SELESAI)

**Hasil Akhir:** `[ ] LULUS` / `[ ] GAGAL`  
**Catatan Bug:** Fase 5.3 belum dapat dipastikan karena belum ada cara memicu timeout, agar laporan berstatus DITUTUP.
