# SCN-09 — SLA Terlewat (Laporan Terlambat)

**Status Akhir SLA:** `TERLAMBAT`  
**Aktor:** Admin Pusat → Admin Dinas → Petugas (lambat menyelesaikan)  
**Estimasi Waktu:** Testing + simulasi DB  
**Prasyarat:** Laporan sudah didisposisi dengan prioritas tertentu.

---

## 🗺️ Alur

```
Admin disposisi laporan dengan prioritas Kritis (SLA = 24 jam)
    → SLA record dibuat, status = BERJALAN
    → Waktu berlalu melebihi deadline
    → Scheduler: checkAndUpdateOverdueSla()
    → SLA status = TERLAMBAT
    → Admin bisa melihat di panel SLA monitoring
```

---

## LANGKAH DETAIL

### FASE 1 — Setup: Disposisi dengan Prioritas Kritis

| # | Aksi | URL | Yang Dicek | Hasil Ekspektasi | ✓/✗ | Catatan |
|---|------|-----|------------|------------------|-----|---------|
| 1.1 | Warga buat laporan | `/warga/create-report` | - | Laporan tersubmit | `[ ]` | ID: _______ |
| 1.2 | Admin pusat approve | `/admin/validation` | - | Status = DIVALIDASI | `[ ]` | |
| 1.3 | Admin pusat disposisi | `/admin/disposisi` | **Pilih prioritas = Kritis** | Disposisi berhasil | `[ ]` | |
| 1.4 | Cek SLA record | `/admin/sla` | 📋 Daftar SLA | SLA muncul dengan deadline = sekarang + 24 jam | `[ ]` | |
| 1.5 | Catat deadline SLA | - | Tanggal/jam deadline | CATAT: _______ | `[ ]` | |

---

### FASE 2 — Simulasi Waktu Terlewat

**Via Database (cepat untuk testing):**

| # | Aksi | SQL | Hasil | ✓/✗ |
|---|------|-----|-------|-----|
| 2.1 | Update SLA deadline ke masa lalu | `UPDATE sla_records SET sla_deadline_at = NOW() - INTERVAL '1 hour' WHERE current_status = 'BERJALAN'` | Deadline = 1 jam lalu | `[ ]` |
| 2.2 | Trigger `checkAndUpdateOverdueSla()` | Restart app / endpoint manual | Scheduler jalan | `[ ]` |

---

### FASE 3 — Verifikasi SLA Terlambat

| # | Aksi | URL | Yang Dicek | Hasil Ekspektasi | ✓/✗ | Catatan |
|---|------|-----|------------|------------------|-----|---------|
| 3.1 | Buka SLA monitoring | `/admin/sla` | 📋 Daftar SLA | SLA muncul dengan status **"TERLAMBAT"** | `[ ]` | |
| 3.2 | Cek lateItems count | - | Counter "Terlambat" | Bertambah | `[ ]` | |
| 3.3 | Cek warna/label terlambat | - | 📸 UI | Label merah/badge "Terlambat" tampil | `[ ]` | |
| 3.4 | Cek di antrean dinas | `/admin/dinas/queue` | Status laporan | Counter "Terlambat SLA" tampil | `[ ]` | |
| 3.5 | Cek status laporan warga | `/warga/report-detail` | Countdown SLA | SLA countdown mungkin 0 atau error | `[ ]` | |

---

### FASE 4 — Pause & Resume SLA

| # | Aksi | URL | Yang Dicek | Hasil Ekspektasi | ✓/✗ | Catatan |
|---|------|-----|------------|------------------|-----|---------|
| 4.1 | Admin dinas tugaskan petugas | `/admin/dinas/penugasan` | - | Petugas ditugaskan | `[ ]` | |
| 4.2 | **Pause SLA** | POST `/admin/dinas/pause-sla` taskId=... reason="alasan" | Flash message | "SLA berhasil dijeda" | `[ ]` | |
| 4.3 | Cek SLA status | - | SLA status | Status = **TERTUNDA** | `[ ]` | |
| 4.4 | Cek SlaPauseLog terbuat | DB | - | Record pause tersimpan dengan waktu & alasan | `[ ]` | |
| 4.5 | **Resume SLA** | POST `/admin/dinas/resume-sla` | Flash message | "SLA berhasil dilanjutkan" | `[ ]` | |
| 4.6 | Cek SLA status kembali BERJALAN | - | SLA status | Status = **BERJALAN** | `[ ]` | |
| 4.7 | ⚠️ Cek deadline diperpanjang | - | SLA deadline | Deadline = deadline lama + durasi pause | `[ ]` | |

---

## ✅ Kriteria LULUS

- [ ] SLA dibuat dengan durasi sesuai prioritas (Kritis = 24 jam)
- [ ] Status SLA berubah ke TERLAMBAT setelah deadline lewat
- [ ] Admin dinas bisa pause dan resume SLA
- [ ] Setelah resume, deadline diperpanjang sejumlah durasi pause

**Hasil Akhir:** `[ ] LULUS` / `[ ] GAGAL`  
**Catatan Bug:** _________________________________
