# Daftar Akun Demo AduAja

## Admin Pusat

| Role | Email | Password | Wilayah | Akses |
|------|-------|----------|---------|-------|
| Admin Pusat Medan | `admin.pusat.medan@aduaja.go.id` | `admin123` | Kota Medan | Laporan, validasi, disposisi, merge, sengketa di wilayah Medan |
| Admin Pusat Pekanbaru | `admin.pusat.pekanbaru@aduaja.go.id` | `admin123` | Kota Pekanbaru | Laporan, validasi, disposisi, merge, sengketa di wilayah Pekanbaru |
| Admin Pusat Tanjungpinang | `admin.pusat.tanjungpinang@aduaja.go.id` | `admin123` | Kota Tanjungpinang | Laporan, validasi, disposisi, merge, sengketa di wilayah Tanjungpinang |

---

## Admin Dinas

### Kota Medan — Kecamatan Medan Baru

| Email | Agency | Petugas |
|-------|--------|---------|
| `admin.pu.medan@aduaja.go.id` | Dinas PU Kota Medan | Ahmad Fauzi |
| `admin.lh.medan@aduaja.go.id` | Dinas LH Kota Medan | Rizal Harahap |
| `admin.perhubungan.medan@aduaja.go.id` | Dinas Perhubungan Kota Medan | Dewi Sartika |

### Kota Pekanbaru — Kecamatan Tampan

| Email | Agency | Petugas |
|-------|--------|---------|
| `admin.pu.pekanbaru@aduaja.go.id` | Dinas PU Kota Pekanbaru | Budi Hartono |
| `admin.lh.pekanbaru@aduaja.go.id` | Dinas LH Kota Pekanbaru | Siti Aminah |
| `admin.perhubungan.pekanbaru@aduaja.go.id` | Dinas Perhubungan Kota Pekanbaru | Joko Susilo |

### Kota Tanjungpinang — Kecamatan Bukit Bestari

| Email | Agency | Petugas |
|-------|--------|---------|
| `admin.pu.tanjungpinang@aduaja.go.id` | Dinas PU Kota Tanjungpinang | Maria Simanjuntak |
| `admin.lh.tanjungpinang@aduaja.go.id` | Dinas LH Kota Tanjungpinang | Andi Pratama |
| `admin.perhubungan.tanjungpinang@aduaja.go.id` | Dinas Perhubungan Kota Tanjungpinang | Lisa Kusuma |

**Password semua admin dinas:** `admin123`

---

## Petugas Lapangan

| Nama | NIP | Agency | Lokasi | Email | Password |
|------|-----|--------|--------|-------|----------|
| Ahmad Fauzi | — | PU Medan | Medan | `ahmad.fauzi@aduaja.go.id` | `petugas123` |
| Rizal Harahap | — | LH Medan | Medan | `rizal.harahap@aduaja.go.id` | `petugas123` |
| Dewi Sartika | — | Perhubungan Medan | Medan | `dewi.sartika@aduaja.go.id` | `petugas123` |
| Budi Hartono | — | PU Pekanbaru | Pekanbaru | `budi.hartono@aduaja.go.id` | `petugas123` |
| Siti Aminah | — | LH Pekanbaru | Pekanbaru | `siti.aminah@aduaja.go.id` | `petugas123` |
| Joko Susilo | — | Perhubungan Pekanbaru | Pekanbaru | `joko.susilo@aduaja.go.id` | `petugas123` |
| Maria Simanjuntak | — | PU Tanjungpinang | Tanjungpinang | `maria.simanjuntak@aduaja.go.id` | `petugas123` |
| Andi Pratama | — | LH Tanjungpinang | Tanjungpinang | `andi.pratama@aduaja.go.id` | `petugas123` |
| Lisa Kusuma | — | Perhubungan Tanjungpinang | Tanjungpinang | `lisa.kusuma@aduaja.go.id` | `petugas123` |

**Password semua petugas:** `petugas123`

---

## Warga (Pelapor)

| Nama | Email | Password |
|------|-------|----------|
| Budi Santoso | `budi.santoso@email.com` | `warga123` |
| Sari Dewi | `sari.dewi@email.com` | `warga123` |
| Agus Setiawan | `agus.setiawan@email.com` | `warga123` |
| Rina Anggraini | `rina.anggraini@email.com` | `warga123` |
| Doni Prasetyo | `doni.prasetyo@email.com` | `warga123` |

---

## Laporan Tersedia (Status: DIVALIDASI — siap disposisi)

| Tiket | Deskripsi | Lokasi | Pelapor |
|-------|-----------|--------|---------|
| ADJ-2026-00001 | Jalan berlubang di Jl. Sudirman | Medan | Budi Santoso |
| ADJ-2026-00002 | Tumpukan sampah di Jl. Gajah Mada | Medan | Sari Dewi |
| ADJ-2026-00003 | Lampu PJU mati di Simpang Tiga | Pekanbaru | Agus Setiawan |
| ADJ-2026-00004 | Taman kota tidak terawat | Pekanbaru | Rina Anggraini |
| ADJ-2026-00005 | Jalan rusak parah di Jl. Merdeka | Tanjungpinang | Doni Prasetyo |
| ADJ-2026-00006 | Sampah berserakan di pasar tradisional | Tanjungpinang | Budi Santoso |

---

## Skenario Test FR-DSP-02 & Region Scoping Admin Pusat

1. Login sebagai **Admin Pusat Medan** (`admin.pusat.medan@aduaja.go.id`) → hanya lihat laporan dari **Medan** (ADJ-00001/ADJ-00002)
2. Buka menu **Disposisi** → pilih laporan dari Medan → daftar dinas tujuan hanya tampil agency yang region-nya **Medan Baru**
3. Login sebagai **Admin Pusat Pekanbaru** (`admin.pusat.pekanbaru@aduaja.go.id`) → hanya lihat laporan dari **Pekanbaru** (ADJ-00003/ADJ-00004)
4. Buka menu **Disposisi** → pilih laporan dari Pekanbaru → daftar dinas tujuan hanya tampil agency yang region-nya **Tampan**
5. Login sebagai **Admin Pusat Tanjungpinang** (`admin.pusat.tanjungpinang@aduaja.go.id`) → hanya lihat laporan dari **Tanjungpinang** (ADJ-00005/ADJ-00006)
6. Login sebagai **Admin Dinas** (contoh: `admin.pu.medan@aduaja.go.id`) → hanya lihat laporan yang didisposisi ke agency-nya

**Password semua admin pusat:** `admin123`
