# SCN-14 — Lupa Password → Reset via OTP

**Aktor:** Warga (punya akun ACTIVE)  
**Estimasi Waktu:** 10 menit

---

## LANGKAH DETAIL

| # | Aksi | URL | Hasil Ekspektasi | ✓/✗ | Catatan |
|---|------|-----|------------------|-----|---------|
| 1.1 | Buka halaman login | `/warga/login` | Link "Lupa Password" ada | `[ ]` | |
| 1.2 | Klik "Lupa Password" | `/warga/forgot-password` | Form input email | `[ ]` | |
| 1.3 | ⚠️ Masukkan email tidak terdaftar | POST `/warga/forgot-password` | Redirect ke verify (tidak bocorkan info) | `[ ]` | Security: jangan bilang "email tidak ada" |
| 1.4 | Masukkan email terdaftar | POST `/warga/forgot-password` | OTP terkirim, redirect ke verify | `[ ]` | |
| 1.5 | Cek email | Inbox | Email OTP reset password | `[ ]` | |
| 1.6 | ⚠️ Masukkan OTP salah | `/warga/verify-otp-reset` | Error OTP | `[ ]` | |
| 1.7 | Masukkan OTP benar | - | Form password baru muncul | `[ ]` | |
| 1.8 | ⚠️ Password baru < 8 karakter | - | Error | "Password minimal 8 karakter" | `[ ]` | |
| 1.9 | ⚠️ Confirm password tidak cocok | - | Error | "Password baru tidak cocok" | `[ ]` | |
| 1.10 | Isi password baru valid + confirm | POST reset | Flash sukses, redirect login | `[ ]` | |
| 1.11 | Login dengan password lama | `/warga/login` | Error | Login gagal (password lama tidak valid) | `[ ]` | |
| 1.12 | Login dengan password baru | `/warga/login` | Berhasil masuk dashboard | `[ ]` | |

---

## ✅ Kriteria LULUS

- [ ] OTP dikirim ke email yang terdaftar
- [ ] Email tidak terdaftar tidak memberi pesan error yang jelas
- [ ] Password baru berhasil diset dan bisa login
- [ ] Password lama tidak bisa dipakai lagi

**Hasil Akhir:** `[ ] LULUS` / `[ ] GAGAL`  
**Catatan Bug:** _________________________________


---

# SCN-15 — Admin Dinas Buat Akun Petugas Baru + Onboarding

**Aktor:** Admin Dinas → Petugas Baru  
**Estimasi Waktu:** 15 menit

---

## 🗺️ Alur

```
Admin Dinas buat akun petugas
    → Petugas login pertama kali
    → Dipaksa ganti password (force change)
    → Petugas bisa check-in dan lihat tugas
```

---

## LANGKAH DETAIL

### FASE 1 — Admin Dinas Buat Akun

| # | Aksi | URL | Yang Dicek | Hasil Ekspektasi | ✓/✗ | Catatan |
|---|------|-----|------------|------------------|-----|---------|
| 1.1 | Login admin dinas | `/admin/login` | - | Masuk | `[ ]` | |
| 1.2 | Buka halaman kelola petugas | `/admin/dinas/petugas` | Daftar petugas | Petugas dinas ini tampil | `[ ]` | |
| 1.3 | ⚠️ Submit form petugas baru tanpa password | POST create | Error | "Password minimal 6 karakter" | `[ ]` | |
| 1.4 | ⚠️ Submit password 5 karakter | - | Error | "Password minimal 6 karakter" | `[ ]` | |
| 1.5 | Isi semua field (nama, email unik, password ≥6) | - | - | Form valid | `[ ]` | |
| 1.6 | **Submit buat petugas** | POST `/admin/dinas/petugas/create` | Flash | "Petugas [nama] berhasil dibuat" | `[ ]` | |
| 1.7 | Cek daftar petugas | `/admin/dinas/petugas` | Daftar | Petugas baru tampil di daftar | `[ ]` | |
| 1.8 | Set NIP + wilayah tugas petugas | POST `/admin/dinas/petugas/update-profile` | Flash | "Profil petugas berhasil diperbarui" | `[ ]` | |

---

### FASE 2 — Petugas Login Pertama Kali

🔄 **Petugas Baru**

| # | Aksi | URL | Yang Dicek | Hasil Ekspektasi | ✓/✗ | Catatan |
|---|------|-----|------------|------------------|-----|---------|
| 2.1 | Login dengan kredensial yang dibuat admin | `/petugas/login` | Redirect | Diarahkan ke `/petugas/change-password` (BUKAN dashboard) | `[ ]` | Force change |
| 2.2 | ⚠️ Coba akses dashboard langsung | `/petugas/dashboard` | Redirect | Kembali ke change-password | `[ ]` | |
| 2.3 | ⚠️ Isi password baru ≠ confirm | POST `/petugas/change-password` | Error | "Konfirmasi password tidak cocok" | `[ ]` | |
| 2.4 | Ganti password dengan password baru yang valid | POST change-password | Flash + redirect | "Password berhasil diubah. Silakan login." → redirect `/petugas/login` | `[ ]` | |
| 2.5 | Login dengan password LAMA | - | Error | Login gagal | `[ ]` | |
| 2.6 | Login dengan password BARU | `/petugas/login` | Dashboard | Masuk ke dashboard petugas | `[ ]` | |
| 2.7 | Cek nama dinas di dashboard | `/petugas/dashboard` | 📋 Nama dinas | Nama dinas sesuai (dari profile, bukan hardcoded) | `[ ]` | FIX-10 |
| 2.8 | Cek check-in | POST check-in dengan koordinat valid | Dashboard | Check-in berhasil | `[ ]` | |
| 2.9 | Cek bisa akses daftar tugas setelah check-in | `/petugas/tasks` | Daftar tugas | Halaman tugas tampil (bukan redirect dashboard) | `[ ]` | |

---

## ✅ Kriteria LULUS

- [ ] Admin dinas bisa buat akun petugas
- [ ] Petugas baru dipaksa ganti password saat login pertama kali
- [ ] Tidak bisa akses halaman lain sebelum ganti password
- [ ] Setelah ganti password, alur normal berjalan

**Hasil Akhir:** `[ ] LULUS` / `[ ] GAGAL`  
**Catatan Bug:** _________________________________
