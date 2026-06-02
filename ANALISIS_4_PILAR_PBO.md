# Analisis 4 Pilar PBO pada Aplikasi AduAja

**AduAja** — Aplikasi Pelaporan Masyarakat berbasis Java Spring Boot

**Kelompok PLR-PEMUDA LEGEND REVOLUTIONER**

| No | Nama | NIM |
|---|---|---|
| 1 | Christein Akadojuanrich Habayaki Purba | 241401012 |
| 2 | El Fahreza Sufi | 241401042 |
| 3 | Cristoval Pratama Siahaan | 241401057 |
| 4 | M. Zidan Ruriano AG | 241401063 |

---

## Daftar Isi

1. [Enkapsulasi (Encapsulation)](#1-enkapsulasi-encapsulation)
2. [Pewarisan (Inheritance)](#2-pewarisan-inheritance)
3. [Polimorfisme (Polymorphism)](#3-polimorfisme-polymorphism)
4. [Abstraksi (Abstraction)](#4-abstraksi-abstraction)

---

## 1. Enkapsulasi (Encapsulation)

Enkapsulasi adalah prinsip menyembunyikan data internal suatu objek dan hanya menyediakan akses melalui metode publik (getter & setter). Berikut implementasinya di aplikasi AduAja:

### 1.1 Entity Model — Semua Field Private

Seluruh **26 entity class** menerapkan enkapsulasi dengan menjadikan semua field sebagai **`private`** dan hanya bisa diakses melalui getter/setter publik.

| File | Field Private |
|---|---|
| `model/User.java` | `userId`, `fullName`, `email`, `phoneNumber`, `passwordHash`, `role`, `accountStatus` |
| `model/Report.java` | `reportId`, `ticketNumber`, `description`, `latitude`, `longitude`, `photoBase64`, `status`, `adminNotes` |
| `model/FieldTask.java` | `taskId`, `status`, `assignedTo`, `notes`, `photoBefore`, `photoAfter` |
| `model/Disposition.java` | `dispositionId`, `fromAdmin`, `toAgency`, `notes`, `dispositionDate` |
| `model/DisputeRecord.java` | `disputeId`, `reason`, `photoEvidence`, `resolution`, `resolvedAt` |
| `model/SlaRecord.java` | `slaId`, `slaStartAt`, `slaDeadlineAt`, `totalPausedMinutes`, `currentStatus` |
| `model/AuditLog.java` | `logId`, `actor`, `actionType`, `oldValue`, `newValue`, `ipAddress` |
| `model/Notification.java` | `notificationId`, `title`, `message`, `isRead`, `recipient` |
| `model/OfficerAttendance.java` | `attendanceId`, `officer`, `checkInTime`, `checkOutTime`, `latitude`, `longitude` |
| `model/MergeRecord.java` | `mergeId`, `parentReport`, `childReport`, `mergedAt`, `mergedBy` |
| `model/ConfirmationRequest.java` | `requestId`, `isConfirmed`, `confirmationDate`, `notes` |
| `model/TaskEvidence.java` | `evidenceId`, `photoBase64`, `description`, `submittedAt` |
| `model/ValidationDecision.java` | `decisionId`, `decision`, `reason`, `decidedAt`, `decidedBy` |

**Contoh kode** — `User.java:18–43`:
```java
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String userId;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;        // ← private: tidak bisa diakses langsung

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;                  // ← private: hanya bisa diubah via setter

    // Getter publik untuk akses terkontrol
    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
```

### 1.2 DTO — Data Transfer Object Sebagai Pembatas

DTO memisahkan data input dari Entity. Semua field dalam **16 DTO class** bersifat private.

| File | Field Private |
|---|---|
| `dto/CreateReportDTO.java` | `description`, `locationHint`, `latitude`, `longitude`, `photoBase64`, `categoryId`, `regionId` |
| `dto/LoginDTO.java` | `email`, `password` |
| `dto/RegisterDTO.java` | `fullName`, `email`, `password`, `phoneNumber` |
| `dto/DispositionDTO.java` | `reportId`, `agencyId`, `notes`, `priority` |
| `dto/DisputeDTO.java` | `reason`, `photoEvidence` |
| `dto/MergeDTO.java` | `parentReportId`, `childReportIds` |
| `dto/ProfileDTO.java` | `fullName`, `phoneNumber`, `address` |
| `dto/ResetPasswordDTO.java` | `token`, `newPassword`, `confirmPassword` |
| `dto/ReportFilterDTO.java` | `status`, `categoryId`, `regionId`, `startDate`, `endDate` |
| `dto/SlaStatusDTO.java` | `reportId`, `slaStatus`, `remainingMinutes` |
| `dto/TaskExecutionDTO.java` | `taskId`, `photoAfter`, `description` |

**Contoh kode** — `CreateReportDTO.java:9–19`:
```java
public class CreateReportDTO {
    private String description;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String photoBase64;
    private String categoryId;

    // Hanya getter & setter publik
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
}
```

### 1.3 Enkapsulasi Tingkat Lanjut — Package-Private Setter

**`AuditLog.java`** menerapkan enkapsulasi yang lebih ketat dengan **package-private setter** (tanpa modifier `public`). Setter hanya bisa diakses oleh class dalam package `model`, seperti `AuditLogFactory`.

```java
// AuditLog.java:61-73 — Setter package-private (tanpa public)
void setLogId(String logId) { this.logId = logId; }       // ← tidak public
void setActor(User actor) { this.actor = actor; }          // ← tidak public
void setActionType(String actionType) { this.actionType = actionType; }
void setOldValue(String oldValue) { this.oldValue = oldValue; }
void setNewValue(String newValue) { this.newValue = newValue; }
```

Audit dari luar hanya bisa dibuat melalui **static factory method** `AuditLog.create()` atau `AuditLogFactory`, sehingga data audit log terproteksi dari modifikasi sembarangan.

### 1.4 Enkapsulasi Logika Bisnis di Service Layer

Service menyembunyikan detail implementasi dari Controller. Contoh di `AuthServiceImpl.java:113–116`:

```java
@Override
public boolean verifyPassword(String rawPassword, String hashedPassword) {
    // ENKAPSULASI: detail BCrypt tersembunyi dari caller
    return passwordEncoder.matches(rawPassword, hashedPassword);
}
```

Controller tidak perlu tahu bagaimana password diverifikasi — cukup panggil `authService.verifyPassword()`.

---

## 2. Pewarisan (Inheritance)

Inheritance adalah mekanisme di mana suatu class mewarisi properti dan method dari class lain. Di AduAja, inheritance diterapkan dalam 3 bentuk:

### 2.1 Inheritance Class — `BaseEntity` sebagai Parent

Semua **26 entity class** mewarisi `BaseEntity` menggunakan keyword `extends`.

| Entity Class | Parent |
|---|---|
| `User.java` | `extends BaseEntity` |
| `Report.java` | `extends BaseEntity` |
| `FieldTask.java` | `extends BaseEntity` |
| `Disposition.java` | `extends BaseEntity` |
| `DisputeRecord.java` | `extends BaseEntity` |
| `SlaRecord.java` | `extends BaseEntity` |
| `AuditLog.java` | `extends BaseEntity` |
| `Notification.java` | `extends BaseEntity` |
| `ActiveSession.java` | `extends BaseEntity` |
| `Agency.java` | `extends BaseEntity` |
| `ConfirmationRequest.java` | `extends BaseEntity` |
| `FieldTaskStatusRevision.java` | `extends BaseEntity` |
| `LoginAttempt.java` | `extends BaseEntity` |
| `MergeRecord.java` | `extends BaseEntity` |
| `OfficerAttendance.java` | `extends BaseEntity` |
| `OtpVerification.java` | `extends BaseEntity` |
| `ReportCategory.java` | `extends BaseEntity` |
| `ReportRevision.java` | `extends BaseEntity` |
| `SlaPauseLog.java` | `extends BaseEntity` |
| `TaskEvidence.java` | `extends BaseEntity` |
| `TaskPostponement.java` | `extends BaseEntity` |
| `UserProfile.java` | `extends BaseEntity` |
| `ValidationDecision.java` | `extends BaseEntity` |

**Diagram inheritance:**
```
            ┌─────────────────────────┐
            │     BaseEntity (abstract)│
            │  - createdAt: LocalDateTime│
            │  - updatedAt: LocalDateTime│
            │  + getCreatedAt()          │
            │  + getUpdatedAt()          │
            └──────────┬────────────────┘
                       │ extends
          ┌────────────┼─────────────┬──────────────────┐
          ▼            ▼             ▼                  ▼
      User.java    Report.java   FieldTask.java    AuditLog.java
   (field: email,  (field: ticket,  (field: assignedTo,  (field: actionType,
    role, ...)      description, ...) photoBefore, ...)   oldValue, ...)

    ... dan 22 entity lainnya ...
```

**Kode `BaseEntity.java:11–37`:**
```java
@MappedSuperclass
public abstract class BaseEntity {
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

Setiap entity mewarisi `createdAt` dan `updatedAt` beserta logic `@PrePersist`/`@PreUpdate` — tanpa perlu menulis ulang.

### 2.2 Inheritance Interface — Service Layer

**18 interface service** diwarisi oleh **18 class implementasi** masing-masing.

| Interface (Parent) | Implementation (Child) |
|---|---|
| `AuthService` | `AuthServiceImpl` |
| `UserService` | `UserServiceImpl` |
| `ReportService` | `ReportServiceImpl` |
| `FieldTaskService` | `FieldTaskServiceImpl` |
| `DispositionService` | `DispositionServiceImpl` |
| `DisputeService` | `DisputeServiceImpl` |
| `NotificationService` | `NotificationServiceImpl` |
| `EmailService` | `EmailServiceImpl` |
| `OtpService` | `OtpServiceImpl` |
| `AgencyService` | `AgencyServiceImpl` |
| `AttendanceService` | `AttendanceServiceImpl` |
| `AuditLogService` | `AuditLogServiceImpl` |
| `ConfirmationService` | `ConfirmationServiceImpl` |
| `MergeRecordService` | `MergeRecordServiceImpl` |
| `SlaMonitoringService` | `SlaMonitoringServiceImpl` |
| `SlaRecordService` | `SlaRecordServiceImpl` |
| `ValidationDecisionService` | `ValidationDecisionServiceImpl` |
| `SupabaseStorageService` | (single class) |

**Pola kode** — setiap service interface memiliki satu implementasi:
```java
// AuthService.java (interface)
public interface AuthService {
    Optional<User> login(LoginDTO dto, String ipAddress);
    Optional<User> loginByEmail(String email, String password, String ipAddress);
    boolean verifyPassword(String rawPassword, String hashedPassword);
    void recordLoginAttempt(String email, boolean success, String ipAddress);
}

// AuthServiceImpl.java (implementasi) — implements AuthService
@Service
public class AuthServiceImpl implements AuthService {
    @Override
    public Optional<User> login(LoginDTO dto, String ipAddress) { ... }
    @Override
    public Optional<User> loginByEmail(...) { ... }
    // ...
}
```

### 2.3 Inheritance Repository — Spring Data JPA

**24 repository interface** mewarisi `JpaRepository` dari Spring Data JPA:

```
UserRepository extends JpaRepository<User, String>
ReportRepository extends JpaRepository<Report, String>
FieldTaskRepository extends JpaRepository<FieldTask, String>
... dan 21 repository lainnya ...
```

Ini mewarisi method bawaan seperti `findAll()`, `findById()`, `save()`, `delete()` tanpa perlu implementasi manual.

---

## 3. Polimorfisme (Polymorphism)

Polimorfisme memungkinkan objek dari tipe berbeda merespons panggilan method yang sama dengan perilaku berbeda. AduAja menerapkan dua jenis polimorfisme:

### 3.1 Run-time Polymorphism (Method Overriding)

Setiap **18 service implementation** melakukan override terhadap method-method interfacenya menggunakan `@Override`.

**Method yang di-override di `AuthServiceImpl`:**

| Method Interface | Deskripsi | Baris |
|---|---|---|
| `login()` | Delegasi ke `loginByEmail` | 41–45 |
| `loginByEmail()` | Validasi email + password + akun lock | 47–84 |
| `loginByPhone()` | Validasi via nomor HP | 86–110 |
| `verifyPassword()` | Verifikasi BCrypt | 112–116 |
| `isAccountLocked()` | Deteksi lockout 30 menit | 118–125 |
| `recordLoginAttempt()` | Simpan riwayat percobaan login | 127–136 |
| `logout()` | Invalidate session | 138–142 |

**Contoh kode — `AuthServiceImpl.java:41–45`:**
```java
@Override  // ← Run-time Polymorphism
public Optional<User> login(LoginDTO dto, String ipAddress) {
    return loginByEmail(dto.getEmail(), dto.getPassword(), ipAddress);
}
```

**Contoh kode — `ReportServiceImpl.java`:**
```java
@Override
public Report createReport(CreateReportDTO dto, String wargaId) {
    // Logic lengkap: validasi, set region, generate ticket, persist
}

@Override
public Report updateStatus(String reportId, Report.ReportStatus newStatus,
                           String notes, String changedBy) {
    // Logic update status 1
}

@Override
public Report updateStatus(String reportId, Report.ReportStatus newStatus,
                           String rejectionReason, String adminNotes,
                           String changedBy) {
    // Logic update status 2 (dengan alasan penolakan)
}
```

### 3.2 Compile-time Polymorphism (Method Overloading)

Method overloading terjadi ketika beberapa method memiliki nama sama tetapi parameter berbeda.

#### a. Method Overloading di `AuthService.java:23–25`
```java
public interface AuthService {
    // Login via DTO
    Optional<User> login(LoginDTO dto, String ipAddress);

    // Login via email — OVERLOAD
    Optional<User> loginByEmail(String email, String password, String ipAddress);

    // Login via nomor HP — OVERLOAD
    Optional<User> loginByPhone(String phone, String password, String ipAddress);
}
```

#### b. Method Overloading di `ReportService.java:32–44`
```java
public interface ReportService {
    // Overload: cari laporan dalam rentang tanggal
    List<Report> getReportsByDateRange(LocalDate start, LocalDate end);

    // Overload: cari + filter status
    List<Report> getReportsByStatusAndDateRange(Report.ReportStatus status,
                                                 LocalDate start, LocalDate end);

    // Overload: update status tanpa rejection reason
    Report updateStatus(String reportId, Report.ReportStatus newStatus,
                        String notes, String changedBy);

    // Overload: update status dengan rejection reason
    Report updateStatus(String reportId, Report.ReportStatus newStatus,
                        String rejectionReason, String adminNotes,
                        String changedBy);
}
```

#### c. Default Method (Backward Compatibility) di `ReportService.java:65–109`
```java
// Default method — bentuk lain dari polimorfisme
default Report updateStatus(String id, Report.ReportStatus status) {
    return updateStatus(id, status, null, "SYSTEM");
}

default List<Report> searchReports(String query) {
    return getAllReports().stream()
        .filter(r -> r.getDescription() != null &&
                     r.getDescription().toLowerCase().contains(query.toLowerCase()))
        .toList();
}

default Report createReport(Report report, String userId) {
    CreateReportDTO dto = new CreateReportDTO();
    dto.setDescription(report.getDescription());
    // ... mapping ...
    return createReport(dto, userId);
}
```

#### d. Factory Method Overloading di `AuditLogFactory.java:10–56`
```java
public class AuditLogFactory {
    // Factory method 1: basic log
    public static AuditLog create(User actor, String actionType,
                                   String oldVal, String newVal) { ... }

    // Factory method 2: log dengan report — OVERLOAD
    public static AuditLog createWithReport(User actor, Report report,
                                             String action, String oldVal,
                                             String newVal) { ... }

    // Factory method 3: log paling lengkap — OVERLOAD
    public static AuditLog createFull(User actor, String targetType,
                                       String targetId, String action,
                                       String oldVal, String newVal,
                                       String ipAddress, String deviceInfo) { ... }
}
```

### 3.3 Instanceof Pattern — Pengecekan Tipe Dinamis

Di controller, role user dicek secara dinamis menggunakan `instanceof`-like pattern untuk menentukan perilaku:

```java
// Contoh pola di AdminPusatController.java
User currentUser = (User) session.getAttribute("user");
if (currentUser.getRole() == User.Role.ADMIN_PUSAT) {
    // perilaku A
} else if (currentUser.getRole() == User.Role.ADMIN_DINAS) {
    // perilaku B
}
```

### 3.4 Enum Polymorphism — Status Berbeda, Perilaku Berbeda

Entity `Report.java:110–114` menggunakan enum `ReportStatus` dengan 14 nilai status:
```java
public enum ReportStatus {
    MENUNGGU_VERIFIKASI, DITOLAK, MENUNGGU_REVISI, DITERIMA, TERGABUNG,
    DALAM_PENINJAUAN, DITUGASKAN, SEDANG_BERJALAN, TERTUNDA, TERLAMBAT,
    MENUNGGU_VALIDASI, SENGKETA, DALAM_EVALUASI_SENGKETA, SELESAI_OTOMATIS, SELESAI
}
```

Setiap status diperlakukan berbeda di service/controller — method `updateStatus()` berperilaku berbeda tergantung status lama dan baru.

---

## 4. Abstraksi (Abstraction)

Abstraksi adalah prinsip menyembunyikan detail implementasi dan hanya menampilkan fungsionalitas esensial. AduAja menerapkan abstraksi di beberapa level:

### 4.1 Abstract Class — `BaseEntity.java`

**`BaseEntity`** adalah abstract class yang menjadi fondasi seluruh entity. Ia menyediakan:
- Properti `createdAt` dan `updatedAt`
- Method lifecycle `@PrePersist` dan `@PreUpdate`
- Getter publik

Class ini tidak bisa di-instantiate langsung — hanya bisa di-*extends*.

```java
@MappedSuperclass
public abstract class BaseEntity {    // ← ABSTRACT CLASS
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();  // Logic otomatis
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();  // Logic otomatis
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

26 entity lain tidak perlu menulis ulang field waktu — cukup `extends BaseEntity`.

### 4.2 Service Interface — Kontrak Tanpa Implementasi

**18 service interface** menyediakan abstraksi penuh. Controller hanya bergantung pada interface, bukan implementasi konkret.

**Contoh — `AuthService.java` (abstraksi auth):**
```java
public interface AuthService {
    // Controller hanya tahu method ini — detail di-hidden
    Optional<User> login(LoginDTO dto, String ipAddress);
    boolean verifyPassword(String rawPassword, String hashedPassword);
    boolean isAccountLocked(String email);
    void recordLoginAttempt(String email, boolean success, String ipAddress);
    void logout(String userId);
}
```

Controller menggunakannya tanpa tahu detail:
```java
@Controller
public class AdminAuthController {
    @Autowired
    private AuthService authService;        // ← Tergantung interface, bukan impl

    public String login(LoginDTO dto, HttpSession session) {
        Optional<User> userOpt = authService.login(dto, request.getRemoteAddr());
        // Tidak peduli bagaimana login diimplementasikan
    }
}
```

**18 Service Interface (Abstraksi):**

| Interface | Fungsi |
|---|---|
| `AuthService` | Login, verifikasi password, lockout detection |
| `UserService` | Registrasi, profil, manajemen user |
| `ReportService` | CRUD laporan, update status, generate tiket |
| `FieldTaskService` | Assign, mulai, selesaikan tugas lapangan |
| `DispositionService` | Disposisi laporan ke dinas |
| `DisputeService` | Sengketa, reassign, tutup sengketa |
| `NotificationService` | Kirim notifikasi, mark as read |
| `EmailService` | Kirim email OTP dan notifikasi |
| `OtpService` | Generate dan verifikasi OTP |
| `AgencyService` | CRUD dinas/instansi |
| `AttendanceService` | Check-in/out petugas + GPS |
| `AuditLogService` | Catat dan riwayat aktivitas |
| `ConfirmationService` | Konfirmasi/sengketa dari warga |
| `MergeRecordService` | Merge tiket duplikat |
| `SlaMonitoringService` | Monitor kepatuhan SLA |
| `SlaRecordService` | Start/pause/resume SLA |
| `ValidationDecisionService` | Validasi/tolak/revisi laporan |
| `SupabaseStorageService` | Upload/hapus foto ke Supabase |

### 4.3 Abstraksi Repository — Spring Data JPA

24 repository interface memanfaatkan abstraksi Spring Data JPA. Cukup dengan extends `JpaRepository<T, ID>`, semua operasi CRUD tersedia tanpa implementasi:

```java
public interface ReportRepository extends JpaRepository<Report, String> {
    // Method query otomatis — Spring Data abstraksi
    List<Report> findByStatus(Report.ReportStatus status);
    Optional<Report> findByTicketNumber(String ticketNumber);
    long countByStatus(Report.ReportStatus status);
    long countByStatusAndRegion_RegionId(Report.ReportStatus status, String regionId);
}
```

Tanpa menulis satu baris implementasi, method seperti `findAll()`, `save()`, `findById()` langsung tersedia.

### 4.4 Abstraksi DTO — Memisahkan Input dari Entity

DTO menyediakan layer abstraksi antara form input dan database. Controller menerima data dari user dalam bentuk DTO, bukan Entity:

```
User Input (form)  →  Controller  →  DTO  →  Service  →  Entity  →  Database
```

Contoh di `WargaController.java`:
```java
@PostMapping("/create-report")
public String createReport(@ModelAttribute CreateReportDTO dto, HttpSession session) {
    // Controller hanya tahu DTO, bukan struktur Entity Report
    Report report = reportService.createReport(dto, wargaId);
    return "redirect:/warga/dashboard";
}
```

### 4.5 Abstraksi Konfigurasi — SecurityConfig

`SecurityConfig.java` mengabstraksi aturan keamanan di satu tempat. Controller tidak perlu mengecek izin — framework yang menangani:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/admin/**").authenticated()
            .requestMatchers("/petugas/**").authenticated()
            .requestMatchers("/warga/**").authenticated()
            .requestMatchers("/", "/index", "/admin/login", "/warga/login").permitAll()
        );
        return http.build();
    }
}
```

---

## Ringkasan

| Pilar | Implementasi |
|---|---|
| **Enkapsulasi** | 26 Entity + 16 DTO: semua field private, akses via getter/setter; Package-private setter + Factory; Logika bisnis (BCrypt, lockout) di Service |
| **Inheritance** | `BaseEntity` → 26 Entity; 18 Interface → 18 `Impl`; 24 Repository → `JpaRepository` |
| **Polimorfisme** | Run-time: `@Override` di 18 Impl; Compile-time: overloading `loginByEmail/Phone()`, `updateStatus()`; Default method + Factory |
| **Abstraksi** | `BaseEntity` (abstract); 18 Service Interface (kontrak); Spring Data JPA (query otomatis); DTO Layer (pisahkan input) |
