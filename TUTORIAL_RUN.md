# Tutorial Menjalankan Aplikasi AduAja

## Daftar Isi
1. [Prasyarat](#1-prasyarat)
2. [Clone & Setup](#2-clone--setup)
3. [Jalankan Aplikasi](#3-jalankan-aplikasi)
4. [Akses Database (H2 Console)](#4-akses-database-h2-console)
5. [Daftar Akun](#5-daftar-akun)
6. [Data Awal (Seeded)](#6-data-awal-seeded)
7. [Panduan Fitur per Role](#7-panduan-fitur-per-role)
8. [Skenario Demo Lengkap](#8-skenario-demo-lengkap)
9. [Troubleshooting](#9-troubleshooting)

---

## 1. Prasyarat

| Software | Versi Minimal |
|---|---|
| Java JDK | 26 |
| Maven | 3.9+ |
| Git | any |
| Browser | Chrome / Firefox / Edge |

Cek instalasi:
```
java -version
mvn --version
```

---

## 2. Clone & Setup

```bash
git clone https://github.com/LAVOTSIRC/AduAja.git
cd AduAja
```

### 2.1 File `.env`

```bash
copy .env.example .env
```

> **Catatan**: File `.env` sudah di `.gitignore` jadi aman tidak ter-push.
> Untuk fitur email (OTP, notifikasi, lupa password) isi `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`.
> Untuk fitur upload foto, isi `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_BUCKET`.

### 2.2 Konfigurasi Minimal (.env)

Pastikan minimal ini terisi:
```
DATABASE_URL=jdbc:h2:file:./data/aduaja;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
DATABASE_USER=sa
DATABASE_PASSWORD=

JPA_DDL_AUTO=update
H2_CONSOLE_ENABLED=true
DEV_MODE=true
```

---

## 3. Jalankan Aplikasi

Terminal:
```bash
mvn spring-boot:run
```

IntelliJ IDEA:
1. File → Open → pilih folder project
2. Tunggu indexing selesai
3. Buka `AduAjaApplication.java` → Run (Shift+F10)
4. Atau jalankan Maven goal: `spring-boot:run`

Aplikasi berjalan di: **http://localhost:8080**

> **Catatan Data Awal**: Saat pertama kali jalan, DataSeeder otomatis mengisi semua data awal (user, wilayah, kategori, laporan contoh). Jika ingin data fresh, hapus folder `data/` lalu restart.

---

## 4. Akses Database (H2 Console)

URL: **http://localhost:8080/h2-console**

| Field | Isi |
|---|---|
| JDBC URL | `jdbc:h2:file:./data/aduaja` |
| User Name | `sa` |
| Password | (kosong) |

Gunakan untuk melihat data langsung di tabel `users`, `reports`, `dispute_records`, dll.

---

## 5. Daftar Akun

### 5.1 Warga (Pelapor)

| Nama | Email | Password |
|---|---|---|
| Budi Santoso | budi.santoso@email.com | warga123 |
| Sari Dewi | sari.dewi@email.com | warga123 |
| Agus Setiawan | agus.setiawan@email.com | warga123 |
| Rina Anggraini | rina.anggraini@email.com | warga123 |
| Doni Prasetyo | doni.prasetyo@email.com | warga123 |

**Login:** http://localhost:8080/warga/login

### 5.2 Admin Pusat

| Nama | Email | Password | Wilayah |
|---|---|---|---|
| Admin Pusat Medan | admin.pusat.medan@aduaja.go.id | admin123 | Kota Medan |
| Admin Pusat Pekanbaru | admin.pusat.pekanbaru@aduaja.go.id | admin123 | Kota Pekanbaru |
| Admin Pusat Tanjungpinang | admin.pusat.tanjungpinang@aduaja.go.id | admin123 | Kota Tanjungpinang |

**Login:** http://localhost:8080/admin/login

### 5.3 Admin Dinas

| Nama | Email | Password | Dinas | Kota |
|---|---|---|---|---|
| Admin PU Medan | admin.pu.medan@aduaja.go.id | admin123 | Dinas PU | Medan |
| Admin LH Medan | admin.lh.medan@aduaja.go.id | admin123 | Dinas LH | Medan |
| Admin Perhubungan Medan | admin.perhubungan.medan@aduaja.go.id | admin123 | Dinas Perhubungan | Medan |
| Admin PU Pekanbaru | admin.pu.pekanbaru@aduaja.go.id | admin123 | Dinas PU | Pekanbaru |
| Admin LH Pekanbaru | admin.lh.pekanbaru@aduaja.go.id | admin123 | Dinas LH | Pekanbaru |
| Admin Perhubungan Pekanbaru | admin.perhubungan.pekanbaru@aduaja.go.id | admin123 | Dinas Perhubungan | Pekanbaru |
| Admin PU Tanjungpinang | admin.pu.tanjungpinang@aduaja.go.id | admin123 | Dinas PU | Tanjungpinang |
| Admin LH Tanjungpinang | admin.lh.tanjungpinang@aduaja.go.id | admin123 | Dinas LH | Tanjungpinang |
| Admin Perhubungan Tanjungpinang | admin.perhubungan.tanjungpinang@aduaja.go.id | admin123 | Dinas Perhubungan | Tanjungpinang |

### 5.4 Petugas Lapangan

| Nama | Email | Password | Dinas | Kota |
|---|---|---|---|---|
| Ahmad Fauzi | ahmad.fauzi@aduaja.go.id | petugas123 | PU Medan | Medan |
| Rizal Harahap | rizal.harahap@aduaja.go.id | petugas123 | LH Medan | Medan |
| Dewi Sartika | dewi.sartika@aduaja.go.id | petugas123 | Perhubungan Medan | Medan |
| Budi Hartono | budi.hartono@aduaja.go.id | petugas123 | PU Pekanbaru | Pekanbaru |
| Siti Aminah | siti.aminah@aduaja.go.id | petugas123 | LH Pekanbaru | Pekanbaru |
| Joko Susilo | joko.susilo@aduaja.go.id | petugas123 | Perhubungan Pekanbaru | Pekanbaru |
| Maria Simanjuntak | maria.simanjuntak@aduaja.go.id | petugas123 | PU Tanjungpinang | Tanjungpinang |
| Andi Pratama | andi.pratama@aduaja.go.id | petugas123 | LH Tanjungpinang | Tanjungpinang |
| Lisa Kusuma | lisa.kusuma@aduaja.go.id | petugas123 | Perhubungan Tanjungpinang | Tanjungpinang |

**Login:** http://localhost:8080/petugas/login

---

## 6. Data Awal (Seeded)

Saat `DataSeeder` jalan, berikut yang langsung tersedia:

| Data | Detail |
|---|---|
| **Wilayah** | Kota Medan, Kec. Medan Baru, Kota Pekanbaru, Kec. Tampan, Kota Tanjungpinang, Kec. Bukit Bestari |
| **Kategori Laporan** | Kerusakan Jalan/Infrastruktur (SLA 72 jam), Fasilitas Penerangan Jalan (48 jam), Pemeliharaan Taman (96 jam), Penanganan Kebersihan (72 jam) |
| **Dinas** | PU, LH, Perhubungan di tiap kota (total 9) |
| **Laporan Contoh (6)** | Semua berstatus `DITERIMA` — siap langsung didisposisi |

**6 Laporan Contoh:**

| Tiket | Deskripsi | Wilayah | Pelapor |
|---|---|---|---|
| ADJ-2026-00001 | Jalan berlubang besar diameter ~50cm di Jl. Sudirman | Medan | Budi Santoso |
| ADJ-2026-00002 | Tumpukan sampah di pinggir Jl. Gajah Mada | Medan | Sari Dewi |
| ADJ-2026-00003 | Tiang lampu PJU mati total di Simpang Tiga | Pekanbaru | Agus Setiawan |
| ADJ-2026-00004 | Taman kota tidak terawat, rumput tinggi | Pekanbaru | Rina Anggraini |
| ADJ-2026-00005 | Jalan rusak parah di Jl. Merdeka | Tanjungpinang | Doni Prasetyo |
| ADJ-2026-00006 | Sampah berserakan di pasar tradisional | Tanjungpinang | Budi Santoso |

---

## 7. Panduan Fitur per Role

### 7.1 Warga (Pelapor)

**Login:** http://localhost:8080/warga/login

**Halaman & Fitur:**

| Menu | URL | Fungsi |
|---|---|---|
| Dashboard | `/warga/dashboard` | Lihat ringkasan & riwayat laporan |
| Buat Laporan | `/warga/create-report` | Buat laporan baru (foto + GPS + peta interaktif) |
| Riwayat | `/warga/history` | Semua tiket laporan yang pernah dibuat |
| Detail Laporan | `/warga/report/{id}` | Info lengkap + timeline status + konfirmasi |
| Profile | `/warga/profile` | Edit profil, ganti password |

**Flow Warga:**
1. **Buat Laporan** → pilih kategori, isi deskripsi, ambil foto (kamera langsung), pilih lokasi di peta/GPS → preview → kirim
2. **Pantau Status** → dashboard menampilkan status terkini tiap laporan
3. **Konfirmasi** → saat petugas selesai, warga dapat Terima (Selesai) atau Tolak (Sengketa)
4. **Ajukan Sengketa** → jika tolak, isi alasan + foto bukti → masuk antrian Admin Dinas

### 7.2 Admin Pusat

**Login:** http://localhost:8080/admin/login

**Halaman & Fitur:**

| Menu | URL | Fungsi |
|---|---|---|
| Dashboard | `/admin/dashboard` | Antrian laporan + ringkasan |
| Validasi | `/admin/validate/{id}` | Detail laporan: Terima/Tolak/Revisi |
| Disposisi | `/admin/disposisi` | Teruskan laporan ke dinas terkait |
| Merge Review | (dari dashboard) | Centang 2+ laporan → bandingkan → merge |
| SLA | `/admin/sla` | Dashboard SLA monitor |
| Laporan | `/admin/laporan` | Semua laporan + filter/search |

**Flow Admin Pusat:**
1. **Validasi Laporan** → buka laporan MENUNGGU_VERIFIKASI → Terima / Tolak (isi alasan) / Minta Revisi
2. **Disposisi** → Dari laporan DITERIMA → pilih dinas tujuan → kirim
3. **Merge** → centang laporan serupa → Review → pilih parent → Merge

### 7.3 Admin Dinas

**Login:** http://localhost:8080/admin/login (sama dengan Admin Pusat, dibedakan oleh role)

**Halaman & Fitur:**

| Menu | URL | Fungsi |
|---|---|---|
| Dashboard | `/admin/dinas/dashboard` | Ringkasan + queue laporan masuk |
| Queue | `/admin/dinas/queue` | Antrian disposisi → assign ke petugas |
| Progress | `/admin/dinas/progress` | Laporan sedang dikerjakan + jeda/resume |
| Sengketa | `/admin/dinas/sengketa` | Queue sengketa → tugaskan kembali / tutup |
| Petugas | `/admin/dinas/petugas` | Manajemen petugas (tambah, status) |
| Laporan | `/admin/dinas/laporan` | Semua laporan + filter |
| SLA | `/admin/dinas/sla` | Monitor SLA + jeda waktu |

**Flow Admin Dinas:**
1. **Assign Petugas** → Queue → pilih laporan → pilih petugas → Konfirmasi
2. **Jeda SLA** → jika perlu, pause SLA dengan alasan → resume nanti
3. **Tangani Sengketa** → Queue sengketa → Tugaskan Kembali ke petugas lain / Tutup Laporan
4. **Reassign** → ganti petugas untuk laporan yang sudah ditugaskan

### 7.4 Petugas Lapangan

**Login:** http://localhost:8080/petugas/login

**Halaman & Fitur:**

| Menu | URL | Fungsi |
|---|---|---|
| Dashboard | `/petugas/dashboard` | To-Do List (Tugas Baru / Sedang Dikerjakan / Tertunda) |
| Detail Tugas | `/petugas/task/{id}` | Detail + peta + countdown SLA |
| Check-In | `/petugas/checkin` | Absen masuk (wajib sebelum mulai tugas) |
| Check-Out | `/petugas/checkout` | Absen pulang |
| Riwayat | `/petugas/history` | Riwayat tugas selesai + performa |

**Flow Petugas:**
1. **Check-In** → absen dengan GPS → baru bisa dapat tugas
2. **Mulai Kerjakan** → buka tugas → klik Mulai (validasi radius 10km)
3. **Selesaikan Tugas** → foto sebelum & sesudah (watermark otomatis) → deskripsi → selesai
4. **Ajukan Penundaan** → jika terkendala, ajukan penundaan dengan alasan → admin approve
5. **Lapor Balik** → jika laporan tidak valid, lapor balik dengan bukti
6. **Check-Out** → akhiri shift

---

## 8. Skenario Demo Lengkap

### Demo 1: Happy Path (Full Flow) — ±7 Menit

**Tujuan**: Menunjukkan alur lengkap dari laporan masuk hingga selesai.

**Step 1 — Login Warga & Buat Laporan (2 menit)**
1. Buka http://localhost:8080/warga/login
2. Email: `budi.santoso@email.com`, Password: `warga123`
3. Klik **Buat Laporan Baru**
4. Izinkan akses kamera & GPS (bisa decline)
5. Klik map untuk set lokasi (atau klik "Gunakan Lokasi Saya")
6. Pilih kategori: **Kerusakan Jalan/Infrastruktur**
7. Isi deskripsi: "Jalan rusak parah di depan sekolah"
8. Ambil foto (atau upload via kamera)
9. Klik **Preview** → **Kirim Laporan**

**Step 2 — Login Admin Pusat & Validasi (1 menit)**
1. Buka http://localhost:8080/admin/login (atau tab baru)
2. Email: `admin.pusat.medan@aduaja.go.id`, Password: `admin123`
3. Di dashboard, cari laporan baru (status MENUNGGU_VERIFIKASI)
4. Klik laporan → **Terima** → status jadi DITERIMA

**Step 3 — Admin Pusat Disposisi (1 menit)**
1. Masih sebagai Admin Pusat, buka menu **Disposisi**
2. Cari laporan yang diterima tadi
3. Klik **Disposisi**, pilih **Dinas PU Kota Medan**
4. Submit → laporan masuk antrian admin dinas

**Step 4 — Login Admin Dinas & Assign (1 menit)**
1. Buka http://localhost:8080/admin/login (login baru atau logout dulu)
2. Email: `admin.pu.medan@aduaja.go.id`, Password: `admin123`
3. Buka menu **Queue**
4. Pilih laporan, klik **Assign**
5. Pilih **Ahmad Fauzi** → konfirmasi → status jadi DITUGASKAN

**Step 5 — Login Petugas & Kerjakan (1 menit)**
1. Buka http://localhost:8080/petugas/login
2. Email: `ahmad.fauzi@aduaja.go.id`, Password: `petugas123`
3. Jika belum check-in, klik **Check-In** dulu
4. Kembali ke dashboard, klik tugas → **Mulai Kerjakan**
5. Klik **Selesaikan Tugas**
6. Isi deskripsi, ambil foto sesudah → **Simpan**

**Step 6 — Login Warga & Konfirmasi (1 menit)**
1. Buka http://localhost:8080/warga/login
2. Email: `budi.santoso@email.com`, Password: `warga123`
3. Buka dashboard, klik laporan yang sudah selesai dikerjakan
4. Status MENUNGGU_VALIDASI → klik **Konfirmasi**
5. Klik **Terima** → status jadi SELESAI ✅

### Demo 2: Sengketa (Dispute Flow) — ±5 Menit

**Tujuan**: Menunjukkan fitur sengketa ketika warga tidak puas.

**Step 1 — Selesaikan tugas (seperti Demo 1 Step 1-5)**

**Step 2 — Warga Tolak (Sengketa)**
1. Login sebagai `budi.santoso@email.com`
2. Buka laporan, klik **Konfirmasi** → **Tolak**
3. Isi alasan: "Perbaikan tidak sesuai, masih berlubang"
4. Upload foto bukti → **Kirim Sengketa**
5. Status laporan jadi SENGKETA

**Step 3 — Admin Dinas Tangani Sengketa**
1. Login sebagai `admin.pu.medan@aduaja.go.id`
2. Buka menu **Sengketa**
3. Klik laporan yang bersengketa
4. Pilih **Tugaskan Kembali** → pilih petugas lain (misal Budi Hartono)
5. Atau pilih **Tutup Laporan** jika sengketa tidak valid

### Demo 3: Merge Tiket (Duplicate Detection) — ±4 Menit

**Tujuan**: Menunjukkan fitur merge untuk laporan duplikat.

1. Login sebagai `admin.pusat.medan@aduaja.go.id`
2. Di dashboard, pastikan ada 2+ laporan DITERIMA
3. Centang 2 laporan yang mirip
4. Klik **Review Terpilih**
5. Bandingkan foto & deskripsi (muncul similarity score + kabupaten dari reverse geocode)
6. Pilih parent → **Merge**
7. Child ticket status jadi TERGABUNG, parent tetap diproses

### Demo 4: Jeda Waktu (Pause SLA) — ±2 Menit

1. Login sebagai `admin.pu.medan@aduaja.go.id`
2. Buka menu **Progress**
3. Pilih laporan yang sedang berjalan
4. Klik **Jeda Waktu**
5. Isi alasan → status jadi TERTUNDA, SLA berhenti
6. Klik **Lanjutkan Waktu** → SLA lanjut, status kembali SEDANG_BERJALAN

### Demo 5: Check-In/Out Petugas — ±2 Menit

1. Login sebagai `ahmad.fauzi@aduaja.go.id`
2. Buka menu **Check-In**
3. Sistem catat lokasi GPS + device info
4. Setelah shift selesai, buka **Check-Out**
5. Cek riwayat absensi di **Riwayat**

### Demo 6: Reassign Petugas — ±2 Menit

1. Login sebagai `admin.pu.medan@aduaja.go.id`
2. Buka menu **Progress**
3. Cari tugas yang sudah diassign
4. Klik **Reassign** → pilih petugas baru
5. Konfirmasi → petugas baru mendapat tugas

---

## 9. Troubleshooting

| Masalah | Solusi |
|---|---|
| `Port 8080 already in use` | Set `PORT=9090` di `.env` |
| `Table not found` / error JPA | Hapus folder `data/` → restart (ddl-auto=update buat ulang) |
| Data seed tidak muncul | Hapus folder `data/` → restart. DataSeeder cuma jalan jika DB kosong |
| Login gagal "Invalid credentials" | Pastikan profile `local` aktif (default). Cek email/password di tabel `users` via H2 console |
| OTP/Email tidak terkirim | Cek `.env`: `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` harus valid |
| Foto tidak tampil / upload gagal | Cek `.env`: `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_BUCKET` |
| Halaman error 500 | Cek log terminal. Biasanya karena data relasi tidak ditemukan. Hapus `data/` lalu restart |
| `LazyInitializationException` di log | Ini dari scheduled task (SLA monitor) — tidak mempengaruhi fungsi utama, aman diabaikan |
| IntelliJ tidak bisa compile | File → Invalidate Caches → Restart |
| Lombok error | Install Lombok plugin (Settings → Plugins) |
| Map tidak muncul | Pastikan koneksi internet untuk load Leaflet/OpenStreetMap tiles |
