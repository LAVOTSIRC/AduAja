# SCN-12 — Warga Batalkan Laporan Sendiri

**Status Akhir Laporan:** `DITOLAK` (oleh warga sendiri)  
**Aktor:** Warga  
**Estimasi Waktu:** 10 menit  
**Prasyarat:** Laporan dalam status MENUNGGU_VALIDASI.

---

## 🗺️ Alur

```
Warga buat laporan → status MENUNGGU_VALIDASI
    → Warga batalkan laporan
    → Status = DITOLAK ("Dibatalkan oleh pelapor")
    → [Tidak bisa aksi apapun lagi]
```

---

## LANGKAH DETAIL

| # | Aksi | URL | Yang Dicek | Hasil Ekspektasi | ✓/✗ | Catatan |
|---|------|-----|------------|------------------|-----|---------|
| 1.1 | Warga login + buat laporan | `/warga/create-report` | - | Status = Menunggu | `[ ]` | ID: _______ |
| 1.2 | Buka detail laporan | `/warga/report-detail?id=...` | 📸 Tombol | Tombol "Batalkan" tersedia | `[ ]` | |
| 1.3 | ⚠️ Coba batalkan laporan milik orang lain | POST `/warga/withdraw-report` id=LAPORAN_ORANG_LAIN | Error | "Anda tidak berwenang membatalkan laporan ini" | `[ ]` | Bug B-05 potensial |
| 1.4 | **Batalkan laporan sendiri** | POST `/warga/withdraw-report` id=ID_SENDIRI | Flash message | "Laporan berhasil dibatalkan." | `[ ]` | |
| 1.5 | Cek status laporan | `/warga/report-detail?id=...` | 📋 Status | Status = **"Ditolak"** | `[ ]` | |
| 1.6 | Cek alasan penolakan | - | 📋 Alasan | "Dibatalkan oleh pelapor" | `[ ]` | |
| 1.7 | Cek tombol aksi hilang | - | 📸 Tombol | Tidak ada tombol lagi | `[ ]` | |
| 1.8 | ⚠️ Coba batalkan laporan yang sudah DIVALIDASI | Login admin → approve laporan → login warga → coba batalkan | Error | "hanya dapat dibatalkan saat masih dalam antrian verifikasi" | `[ ]` | |
| 1.9 | Cek di antrian admin | Login admin → `/admin/validation` | Daftar antrian | Laporan sudah tidak ada di antrian admin | `[ ]` | |
| 1.10 | Cek riwayat warga | `/warga/report-history` | Filter ditolak | Laporan tampil dengan status "Ditolak" | `[ ]` | |

---

## ✅ Kriteria LULUS

- [ ] Pembatalan berhasil saat status MENUNGGU_VALIDASI
- [ ] Status berubah ke DITOLAK dengan alasan "Dibatalkan oleh pelapor"
- [ ] Tidak bisa batalkan laporan yang sudah DIVALIDASI atau lebih jauh
- [ ] Tidak bisa batalkan laporan milik orang lain

**Hasil Akhir:** `[ ] LULUS` / `[ ] GAGAL`  
**Catatan Bug:** _________________________________


---

# SCN-13 — Registrasi Warga Baru → Aktivasi OTP

**Status Akhir Akun:** `ACTIVE`  
**Aktor:** Warga baru (belum punya akun)  
**Estimasi Waktu:** 10 menit  
**Prasyarat:** Email server berfungsi atau mock SMTP tersambung.

---

## 🗺️ Alur

```
Buka halaman register
    → Isi form: NIK, nama, email baru, password, konfirmasi
    → Akun dibuat dengan status PENDING
    → OTP dikirim ke email
    → Masukkan OTP di halaman verifikasi
    → Akun menjadi ACTIVE
    → Login pertama kali
```

---

## LANGKAH DETAIL

| # | Aksi | URL | Yang Dicek | Hasil Ekspektasi | ✓/✗ | Catatan |
|---|------|-----|------------|------------------|-----|---------|
| 1.1 | Buka halaman register | `/warga/register` | Form register | Form dengan field NIK, nama, email, password tampil | `[ ]` | |
| 1.2 | ⚠️ Submit NIK 15 digit | - | Validasi | Error "NIK harus 16 digit angka" | `[ ]` | |
| 1.3 | ⚠️ Submit password tidak match | - | Validasi | Error "Password tidak cocok" | `[ ]` | |
| 1.4 | ⚠️ Submit password < 8 karakter | - | Validasi | Error password terlalu pendek | `[ ]` | |
| 1.5 | Isi semua field valid (NIK 16 digit, email baru) | - | - | Form valid | `[ ]` | |
| 1.6 | **Submit registrasi** | POST `/warga/register` | Redirect | Redirect ke halaman verifikasi OTP | `[ ]` | |
| 1.7 | Cek email masuk | Cek inbox | Email OTP | Email OTP diterima | `[ ]` | |
| 1.8 | ⚠️ Masukkan OTP salah | `/warga/verify-otp` | Error | "Kode OTP tidak valid atau sudah kadaluarsa" | `[ ]` | |
| 1.9 | Masukkan OTP benar | `/warga/verify-otp` | Flash + redirect | "Akun berhasil diverifikasi", redirect ke login | `[ ]` | |
| 1.10 | Login dengan email + password baru | `/warga/login` | Redirect dashboard | Berhasil masuk ke dashboard | `[ ]` | |
| 1.11 | Cek dashboard kosong | `/warga/dashboard` | Angka laporan | Semua angka = 0 (akun baru) | `[ ]` | |
| 1.12 | ⚠️ Uji registrasi ulang dengan email yang sama (ACTIVE) | `/warga/register` | Error | "Email sudah terdaftar" | `[ ]` | |
| 1.13 | ⚠️ Uji registrasi ulang email PENDING (belum OTP) | Daftar lagi dengan email yg statusnya PENDING | Perilaku | Update data + kirim OTP baru (bukan error) | `[ ]` | FR-WRG-02 |

---

## ✅ Kriteria LULUS

- [ ] Form validasi berfungsi (NIK, password)
- [ ] Akun dibuat dengan status PENDING
- [ ] OTP dikirim ke email
- [ ] Setelah OTP benar, akun menjadi ACTIVE
- [ ] Login berhasil dengan akun baru
- [ ] Email yang sudah ACTIVE tidak bisa register ulang

**Hasil Akhir:** `[ ] LULUS` / `[ ] GAGAL`  
**Catatan Bug:** _________________________________
