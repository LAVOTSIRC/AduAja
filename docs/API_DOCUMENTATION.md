# AduAja API Documentation

Base URL: `http://localhost:8080/api`

---

## 1. Reports (Laporan)

### GET `/api/reports`
**Deskripsi**: Mengambil semua laporan
**Response**: `200 OK` - Array of Report

### GET `/api/reports/{id}`
**Deskripsi**: Mengambil laporan berdasarkan ID
**Path Params**: `id` (String)
**Response**: `200 OK` - Report object

### GET `/api/reports/status/{status}`
**Deskripsi**: Mengambil laporan berdasarkan status
**Path Params**: `status` (MENUNGGU, DIVALIDASI, DITOLAK, DIPROSES, SELESAI, SENGKETA)
**Response**: `200 OK` - Array of Report

### GET `/api/reports/user/{userId}`
**Deskripsi**: Mengambil laporan berdasarkan user ID
**Path Params**: `userId` (String)
**Response**: `200 OK` - Array of Report

### GET `/api/reports/disposisi-ready`
**Deskripsi**: Mengambil laporan yang siap didisposisi
**Response**: `200 OK` - Array of Report

### GET `/api/reports/search?q=keyword`
**Deskripsi**: Cari laporan berdasarkan judul/deskripsi
**Query Params**: `q` (String)
**Response**: `200 OK` - Array of Report

### POST `/api/reports?userId=USER_ID`
**Deskripsi**: Buat laporan baru
**Query Params**: `userId` (String)
**Body**: JSON Report
```json
{
  "title": "Jalan Berlubang di Jl. Sudirman",
  "description": "Lubang besar diameter 50cm",
  "category": "Infrastruktur Jalan",
  "location": "Jl. Sudirman, Medan Kota",
  "latitude": "3.5891",
  "longitude": "98.6738",
  "landmark": "Dekat Indomaret"
}
```
**Response**: `201 Created` - Report object

### POST `/api/reports/{id}/validate`
**Deskripsi**: Validasi laporan (approve/reject)
**Path Params**: `id` (String)
**Query Params**: 
- `approved` (boolean)
- `rejectionReason` (String, optional)
- `validatedBy` (String, user ID)
**Response**: `200 OK` - Report object

### PATCH `/api/reports/{id}/status?status=STATUS`
**Deskripsi**: Update status laporan
**Path Params**: `id` (String)
**Query Params**: `status` (Report.Status enum)
**Response**: `200 OK` - Report object

### DELETE `/api/reports/{id}`
**Deskripsi**: Hapus/tolak laporan
**Path Params**: `id` (String)
**Response**: `204 No Content`

### GET `/api/reports/count`
**Deskripsi**: Jumlah laporan per status
**Response**: `200 OK`
```json
{
  "menunggu": 5,
  "divalidasi": 2,
  "diproses": 3,
  "selesai": 10,
  "ditolak": 1
}
```

### POST `/api/reports/{id}/confirm?confirmedBy=USER_ID`
**Deskripsi**: Konfirmasi penyelesaian oleh warga
**Path Params**: `id` (String)
**Query Params**: `confirmedBy` (String)
**Response**: `200 OK` - Report object

### POST `/api/reports/{id}/dispute?disputedBy=USER_ID`
**Deskripsi**: Ajukan sengketa atas laporan
**Path Params**: `id` (String)
**Query Params**: `disputedBy` (String)
**Response**: `200 OK` - Report object

### POST `/api/reports/{id}/revisions?description=TEXT`
**Deskripsi**: Submit revisi laporan
**Path Params**: `id` (String)
**Query Params**: `description` (String)
**Response**: `200 OK` - Report object

---

## 2. Tickets

### GET `/api/tickets`
**Deskripsi**: Mengambil semua tiket
**Response**: `200 OK` - Array of Ticket

### GET `/api/tickets/{id}`
**Deskripsi**: Mengambil tiket berdasarkan ID
**Path Params**: `id` (String)
**Response**: `200 OK` - Ticket object

### GET `/api/tickets/number/{ticketNumber}`
**Deskripsi**: Mengambil tiket berdasarkan nomor tiket
**Path Params**: `ticketNumber` (String)
**Response**: `200 OK` - Ticket object

### GET `/api/tickets/status/{status}`
**Deskripsi**: Mengambil tiket berdasarkan status
**Path Params**: `status` (BARU, IN_PROGRESS, PENDING, SELESAI, DIBATALKAN, ESKALASI)
**Response**: `200 OK` - Array of Ticket

### GET `/api/tickets/petugas/{petugasId}`
**Deskripsi**: Mengambil tiket berdasarkan petugas
**Path Params**: `petugasId` (String)
**Response**: `200 OK` - Array of Ticket

### GET `/api/tickets/petugas/{petugasId}/active`
**Deskripsi**: Mengambil tiket aktif petugas
**Path Params**: `petugasId` (String)
**Response**: `200 OK` - Array of Ticket

### GET `/api/tickets/{ticketId}/progress`
**Deskripsi**: Mengambil riwayat progress tiket
**Path Params**: `ticketId` (String)
**Response**: `200 OK` - Array of TicketProgress

### GET `/api/tickets/{ticketId}/materials`
**Deskripsi**: Mengambil material yang digunakan
**Path Params**: `ticketId` (String)
**Response**: `200 OK` - Array of MaterialUsage

### POST `/api/tickets`
**Deskripsi**: Buat tiket baru
**Query Params**: `reportId`, `petugasId`, `assignedById` (all String)
**Response**: `201 Created` - Ticket object

### POST `/api/tickets/{id}/start?petugasId=USER_ID`
**Deskripsi**: Mulai pengerjaan tiket
**Path Params**: `id` (String)
**Query Params**: `petugasId` (String)
**Response**: `200 OK` - Ticket object

### POST `/api/tickets/{id}/complete?notes=TEXT`
**Deskripsi**: Selesaikan tiket
**Path Params**: `id` (String)
**Query Params**: `notes` (String)
**Response**: `200 OK` - Ticket object

### POST `/api/tickets/{id}/pending?reason=TEXT`
**Deskripsi**: Tunda tiket
**Path Params**: `id` (String)
**Query Params**: `reason` (String)
**Response**: `200 OK` - Ticket object

### POST `/api/tickets/{id}/pause?reason=TEXT`
**Deskripsi**: Jeda tiket (FR-JDA)
**Path Params**: `id` (String)
**Query Params**: `reason` (String)
**Response**: `200 OK` - Ticket object

### POST `/api/tickets/{id}/resume`
**Deskripsi**: Lanjutkan tiket yang dijeda
**Path Params**: `id` (String)
**Response**: `200 OK` - Ticket object

### POST `/api/tickets/{id}/escalate`
**Deskripsi**: Eskalasi tiket (FR-ESK)
**Path Params**: `id` (String)
**Response**: `200 OK` - Ticket object

### POST `/api/tickets/{id}/progress`
**Deskripsi**: Tambahkan progress ke tiket
**Path Params**: `id` (String)
**Query Params**: `petugasId`, `keterangan`, `estimasi` (all String)
**Response**: `200 OK` - Ticket object

### POST `/api/tickets/{id}/materials`
**Deskripsi**: Tambahkan material ke tiket
**Path Params**: `id` (String)
**Query Params**: `materialName`, `quantity`, `unit` (String, Integer, String)
**Response**: `200 OK` - Ticket object

### PATCH `/api/tickets/{id}/status?status=STATUS`
**Deskripsi**: Update status tiket
**Path Params**: `id` (String)
**Query Params**: `status` (Ticket.Status enum)
**Response**: `200 OK` - Ticket object

### GET `/api/tickets/count`
**Deskripsi**: Jumlah tiket per status
**Response**: `200 OK`
```json
{
  "baru": 2,
  "in_progress": 1,
  "pending": 0,
  "selesai": 5
}
```

---

## 3. Users

### GET `/api/users`
**Deskripsi**: Mengambil semua user
**Response**: `200 OK` - Array of User

### GET `/api/users/{id}`
**Deskripsi**: Mengambil user berdasarkan ID
**Path Params**: `id` (String)
**Response**: `200 OK` - User object

### GET `/api/users/username/{username}`
**Deskripsi**: Mengambil user berdasarkan username
**Path Params**: `username` (String)
**Response**: `200 OK` - User object

### GET `/api/users/role/{role}`
**Deskripsi**: Mengambil user berdasarkan role
**Path Params**: `role` (WARGA, ADMIN_PUSAT, ADMIN_DINAS, PETUGAS)
**Response**: `200 OK` - Array of User

### GET `/api/users/dinas/{dinasId}/petugas`
**Deskripsi**: Mengambil petugas berdasarkan dinas
**Path Params**: `dinasId` (String)
**Response**: `200 OK` - Array of User

### POST `/api/users`
**Deskripsi**: Buat user baru
**Body**: JSON User
**Response**: `201 Created` - User object

### PUT `/api/users/{id}`
**Deskripsi**: Update user
**Path Params**: `id` (String)
**Body**: JSON User
**Response**: `200 OK` - User object

---

## 4. Disposisi

### GET `/api/disposisi`
**Deskripsi**: Mengambil semua disposisi
**Response**: `200 OK` - Array of Disposisi

### GET `/api/disposisi/{id}`
**Deskripsi**: Mengambil disposisi berdasarkan ID
**Path Params**: `id` (String)
**Response**: `200 OK` - Disposisi object

### GET `/api/disposisi/status/{status}`
**Deskripsi**: Mengambil disposisi berdasarkan status
**Path Params**: `status` (MENUNGGU, DITERIMA, DITOLAK)
**Response**: `200 OK` - Array of Disposisi

### GET `/api/disposisi/dinas/{dinasId}`
**Deskripsi**: Mengambil disposisi berdasarkan dinas
**Path Params**: `dinasId` (String)
**Response**: `200 OK` - Array of Disposisi

### POST `/api/disposisi`
**Deskripsi**: Buat disposisi baru
**Query Params**: `reportId`, `dinasId`, `assignedById`, `catatan` (optional), `deadline` (optional)
**Response**: `201 Created` - Disposisi object

### PATCH `/api/disposisi/{id}/status?status=STATUS`
**Deskripsi**: Update status disposisi
**Path Params**: `id` (String)
**Query Params**: `status` (Disposisi.Status enum)
**Response**: `200 OK` - Disposisi object

### GET `/api/disposisi/dinas`
**Deskripsi**: Mengambil semua dinas
**Response**: `200 OK` - Array of Dinas

### GET `/api/disposisi/dinas/search?category=KATEGORI`
**Deskripsi**: Cari dinas berdasarkan kategori
**Query Params**: `category` (String)
**Response**: `200 OK` - Array of Dinas

---

## 5. Sengketa

### GET `/api/sengketa`
**Deskripsi**: Mengambil semua sengketa
**Response**: `200 OK` - Array of Sengketa

### GET `/api/sengketa/{id}`
**Deskripsi**: Mengambil sengketa berdasarkan ID
**Path Params**: `id` (String)
**Response**: `200 OK` - Sengketa object

### GET `/api/sengketa/number/{number}`
**Deskripsi**: Mengambil sengketa berdasarkan nomor
**Path Params**: `number` (String)
**Response**: `200 OK` - Sengketa object

### GET `/api/sengketa/status/{status}`
**Deskripsi**: Mengambil sengketa berdasarkan status
**Path Params**: `status` (MENUNGGU_TINJAUAN, DIPROSES, DISETUJUI, DITOLAK)
**Response**: `200 OK` - Array of Sengketa

### POST `/api/sengketa`
**Deskripsi**: Buat sengketa baru
**Query Params**: `ticketId`, `reportId`, `filedById`, `alasan`, `prioritas` (optional)
**Response**: `201 Created` - Sengketa object

### POST `/api/sengketa/{id}/resolve`
**Deskripsi**: Selesaikan sengketa
**Path Params**: `id` (String)
**Query Params**: `keputusan`, `catatan`, `resolvedById` (all String)
**Response**: `200 OK` - Sengketa object

---

## 6. Attendance (Absensi)

### GET `/api/attendance`
**Deskripsi**: Mengambil semua data absensi
**Response**: `200 OK` - Array of Attendance

### GET `/api/attendance/user/{userId}`
**Deskripsi**: Mengambil absensi berdasarkan user
**Path Params**: `userId` (String)
**Response**: `200 OK` - Array of Attendance

### GET `/api/attendance/user/{userId}/active`
**Deskripsi**: Mengambil absensi aktif user
**Path Params**: `userId` (String)
**Response**: `200 OK` - Attendance object

### POST `/api/attendance/checkin`
**Deskripsi**: Check-in petugas
**Query Params**: `userId`, `latitude` (optional), `longitude` (optional), `address` (optional), `deviceInfo` (optional), `ipAddress` (optional)
**Response**: `201 Created` - Attendance object

### POST `/api/attendance/checkout/{attendanceId}`
**Deskripsi**: Check-out petugas
**Path Params**: `attendanceId` (String)
**Query Params**: `latitude` (optional), `longitude` (optional), `address` (optional)
**Response**: `200 OK` - Attendance object

---

## 7. Notifications

### GET `/api/notifications/user/{userId}`
**Deskripsi**: Mengambil notifikasi user
**Path Params**: `userId` (String)
**Response**: `200 OK` - Array of Notification

### GET `/api/notifications/user/{userId}/unread`
**Deskripsi**: Mengambil notifikasi belum dibaca
**Path Params**: `userId` (String)
**Response**: `200 OK` - Array of Notification

### GET `/api/notifications/user/{userId}/unread/count`
**Deskripsi**: Jumlah notifikasi belum dibaca
**Path Params**: `userId` (String)
**Response**: `200 OK` - Long (count)

### POST `/api/notifications`
**Deskripsi**: Buat notifikasi baru
**Query Params**: `userId`, `title`, `message`, `type` (Notification.Type enum)
**Response**: `201 Created` - Notification object

### POST `/api/notifications/{id}/read`
**Deskripsi**: Tandai notifikasi sebagai dibaca
**Path Params**: `id` (String)
**Response**: `200 OK` - Notification object

### POST `/api/notifications/user/{userId}/read-all`
**Deskripsi**: Tandai semua notifikasi sebagai dibaca
**Path Params**: `userId` (String)
**Response**: `200 OK`

---

## 8. Admin & Merge Clusters

### GET `/api/merge-clusters`
**Deskripsi**: Mengambil semua cluster merge
**Response**: `200 OK` - Array of MergeCluster

### GET `/api/merge-clusters/status/{status}`
**Deskripsi**: Mengambil cluster berdasarkan status
**Path Params**: `status` (PENDING, MERGED, CANCELLED)
**Response**: `200 OK` - Array of MergeCluster

### GET `/api/merge-clusters/{id}`
**Deskripsi**: Mengambil cluster berdasarkan ID
**Path Params**: `id` (String)
**Response**: `200 OK` - MergeCluster object

### POST `/api/merge-clusters`
**Deskripsi**: Buat cluster merge baru
**Query Params**: `parentReportId`, `childReportIds` (List<String>), `similarityScore` (optional)
**Response**: `201 Created` - MergeCluster object

### POST `/api/merge-clusters/{id}/merge?mergedById=USER_ID`
**Deskripsi**: Eksekusi merge cluster
**Path Params**: `id` (String)
**Query Params**: `mergedById` (String)
**Response**: `200 OK` - MergeCluster object

### POST `/api/merge-clusters/{id}/cancel`
**Deskripsi**: Batalkan cluster merge
**Path Params**: `id` (String)
**Response**: `200 OK` - MergeCluster object

### GET `/api/merge-clusters/detect?threshold=60`
**Deskripsi**: Deteksi laporan duplikat otomatis
**Query Params**: `threshold` (double, default 60)
**Response**: `200 OK` - Array of List<Report>

---

## 9. SLA Monitoring

### GET `/api/sla/statistics`
**Deskripsi**: Statistik SLA
**Response**: `200 OK`
```json
{
  "totalReports": 10,
  "totalTickets": 5,
  "onTimeReports": 8,
  "lateReports": 2,
  "onTimeTickets": 4,
  "lateTickets": 1
}
```

### GET `/api/sla/late-items`
**Deskripsi**: Daftar item yang terlambat SLA
**Response**: `200 OK` - Array of {type, id, title, status, slaDeadline, hoursLate}

---

## Cara Import ke Postman

1. Buka Postman → Import
2. Pilih "Raw text" atau buat Collection baru
3. Buat folder "AduAja API"
4. Copy-paste setiap endpoint sesuai dokumentasi di atas
5. Set Base URL: `http://localhost:8080/api`
6. Untuk testing form submission, bisa pakai `http://localhost:8080/warga/create-report` dengan method POST dan body `x-www-form-urlencoded`
