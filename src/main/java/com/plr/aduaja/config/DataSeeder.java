package com.plr.aduaja.config;

import com.plr.aduaja.model.*;
import com.plr.aduaja.model.Report.ReportStatus;
import com.plr.aduaja.model.FieldTask.TaskStatus;
import com.plr.aduaja.model.Region.RegionLevel;
import com.plr.aduaja.model.OfficerAttendance.ShiftStatus;
import com.plr.aduaja.model.SlaRecord.SlaStatus;
import com.plr.aduaja.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AgencyRepository agencyRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportCategoryRepository categoryRepository;

    @Autowired
    private FieldTaskRepository fieldTaskRepository;

    @Autowired
    private SlaRecordRepository slaRecordRepository;

    @Autowired
    private OfficerAttendanceRepository attendanceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        Region kotaMedan = new Region();
        kotaMedan.setRegionName("Kota Medan");
        kotaMedan.setRegionLevel(RegionLevel.KOTA);
        regionRepository.save(kotaMedan);

        Region kecMedanBaru = new Region();
        kecMedanBaru.setRegionName("Kecamatan Medan Baru");
        kecMedanBaru.setRegionLevel(RegionLevel.KECAMATAN);
        kecMedanBaru.setParentRegion(kotaMedan);
        regionRepository.save(kecMedanBaru);

        Agency dinasPU = new Agency();
        dinasPU.setAgencyName("Dinas PU dan Penataan Ruang");
        dinasPU.setRegion(kecMedanBaru);
        dinasPU.setContactEmail("pu@medankota.go.id");
        dinasPU.setIsActive(true);
        agencyRepository.save(dinasPU);

        Agency dinasLH = new Agency();
        dinasLH.setAgencyName("Dinas Lingkungan Hidup");
        dinasLH.setRegion(kecMedanBaru);
        dinasLH.setContactEmail("lh@medankota.go.id");
        dinasLH.setIsActive(true);
        agencyRepository.save(dinasLH);

        Agency dinasESDM = new Agency();
        dinasESDM.setAgencyName("Dinas ESDM");
        dinasESDM.setRegion(kecMedanBaru);
        dinasESDM.setContactEmail("esdm@medankota.go.id");
        dinasESDM.setIsActive(true);
        agencyRepository.save(dinasESDM);

        ReportCategory catJalan = new ReportCategory();
        catJalan.setCategoryName("Infrastruktur Jalan");
        catJalan.setSlaDurationHours(72);
        catJalan.setDescription("Laporan kerusakan jalan, lubang, retak");
        categoryRepository.save(catJalan);

        ReportCategory catLampu = new ReportCategory();
        catLampu.setCategoryName("Penerangan Jalan");
        catLampu.setSlaDurationHours(48);
        catLampu.setDescription("Laporan lampu PJU mati, rusak");
        categoryRepository.save(catLampu);

        ReportCategory catDrainase = new ReportCategory();
        catDrainase.setCategoryName("Drainase");
        catDrainase.setSlaDurationHours(96);
        catDrainase.setDescription("Laporan drainase tersumbat, banjir");
        categoryRepository.save(catDrainase);

        User adminPusat = new User();
        adminPusat.setFullName("Admin Pusat");
        adminPusat.setEmail("admin@aduaja.go.id");
        adminPusat.setPasswordHash(passwordEncoder.encode("admin123"));
        adminPusat.setRole(User.Role.ADMIN_PUSAT);
        adminPusat.setAccountStatus(User.AccountStatus.ACTIVE);
        userRepository.save(adminPusat);

        User adminDinas = new User();
        adminDinas.setFullName("Admin Dinas PU");
        adminDinas.setEmail("admin.pu@aduaja.go.id");
        adminDinas.setPasswordHash(passwordEncoder.encode("admin123"));
        adminDinas.setRole(User.Role.ADMIN_DINAS);
        adminDinas.setAccountStatus(User.AccountStatus.ACTIVE);
        userRepository.save(adminDinas);

        User petugas1 = new User();
        petugas1.setFullName("Ahmad Fauzi");
        petugas1.setEmail("ahmad.fauzi@aduaja.go.id");
        petugas1.setPhoneNumber("081234567890");
        petugas1.setPasswordHash(passwordEncoder.encode("petugas123"));
        petugas1.setRole(User.Role.PETUGAS);
        petugas1.setAccountStatus(User.AccountStatus.ACTIVE);
        userRepository.save(petugas1);

        User petugas2 = new User();
        petugas2.setFullName("Rizal Harahap");
        petugas2.setEmail("rizal.harahap@aduaja.go.id");
        petugas2.setPhoneNumber("082345678901");
        petugas2.setPasswordHash(passwordEncoder.encode("petugas123"));
        petugas2.setRole(User.Role.PETUGAS);
        petugas2.setAccountStatus(User.AccountStatus.ACTIVE);
        userRepository.save(petugas2);

        User warga1 = new User();
        warga1.setFullName("Budi Santoso");
        warga1.setEmail("budi.santoso@email.com");
        warga1.setPhoneNumber("081111111111");
        warga1.setPasswordHash(passwordEncoder.encode("warga123"));
        warga1.setRole(User.Role.WARGA);
        warga1.setAccountStatus(User.AccountStatus.ACTIVE);
        userRepository.save(warga1);

        User warga2 = new User();
        warga2.setFullName("Sari Dewi");
        warga2.setEmail("sari.dewi@email.com");
        warga2.setPhoneNumber("082222222222");
        warga2.setPasswordHash(passwordEncoder.encode("warga123"));
        warga2.setRole(User.Role.WARGA);
        warga2.setAccountStatus(User.AccountStatus.ACTIVE);
        userRepository.save(warga2);

        Report report1 = new Report();
        report1.setTicketNumber("ADJ-2026-00001");
        report1.setDescription("Jalan berlubang besar diameter ±50cm, kedalaman ±15cm. Sudah terjadi sejak 2 minggu lalu dan semakin membesar.");
        report1.setCategory(catJalan);
        report1.setRegion(kecMedanBaru);
        report1.setStatus(ReportStatus.DITUGASKAN);
        report1.setReporter(warga1);
        report1.setLocationHint("Jl. Sudirman, Medan Kota");
        report1.setLatitude(new BigDecimal("3.58910000"));
        report1.setLongitude(new BigDecimal("98.67380000"));
        report1.setSubmittedAt(LocalDateTime.now().minusDays(14));
        report1.setUpdatedAt(LocalDateTime.now());
        reportRepository.save(report1);

        Report report2 = new Report();
        report2.setTicketNumber("ADJ-2026-00002");
        report2.setDescription("Tiang lampu nomor 3 dari ujung jalan tidak menyala sejak 1 bulan terakhir.");
        report2.setCategory(catLampu);
        report2.setRegion(kecMedanBaru);
        report2.setStatus(ReportStatus.SELESAI);
        report2.setReporter(warga2);
        report2.setLocationHint("Gang Melati, Medan Baru");
        report2.setLatitude(new BigDecimal("3.58230000"));
        report2.setLongitude(new BigDecimal("98.67010000"));
        report2.setSubmittedAt(LocalDateTime.now().minusDays(30));
        report2.setUpdatedAt(LocalDateTime.now());
        reportRepository.save(report2);

        Report report3 = new Report();
        report3.setTicketNumber("ADJ-2026-00003");
        report3.setDescription("Drainase tersumbat sampah dan lumpur, menyebabkan genangan air saat hujan deras.");
        report3.setCategory(catDrainase);
        report3.setRegion(kecMedanBaru);
        report3.setStatus(ReportStatus.MENUNGGU_VALIDASI);
        report3.setReporter(warga1);
        report3.setLocationHint("Jl. Gatot Subroto, Medan Petisah");
        report3.setLatitude(new BigDecimal("3.59120000"));
        report3.setLongitude(new BigDecimal("98.66450000"));
        report3.setSubmittedAt(LocalDateTime.now().minusDays(2));
        report3.setUpdatedAt(LocalDateTime.now());
        reportRepository.save(report3);

        SlaRecord sla1 = new SlaRecord();
        sla1.setReport(report1);
        sla1.setSlaStartAt(LocalDateTime.now().minusDays(12));
        sla1.setSlaDeadlineAt(LocalDateTime.now().plusDays(3));
        sla1.setCurrentStatus(SlaStatus.BERJALAN);
        sla1.setTotalPausedMinutes(0);
        slaRecordRepository.save(sla1);

        SlaRecord sla2 = new SlaRecord();
        sla2.setReport(report2);
        sla2.setSlaStartAt(LocalDateTime.now().minusDays(28));
        sla2.setSlaDeadlineAt(LocalDateTime.now().minusDays(2));
        sla2.setCurrentStatus(SlaStatus.SELESAI);
        sla2.setCompletedAt(LocalDateTime.now().minusDays(5));
        sla2.setTotalPausedMinutes(0);
        slaRecordRepository.save(sla2);

        FieldTask task1 = new FieldTask();
        task1.setReport(report1);
        task1.setOfficer(petugas1);
        task1.setAssignedBy(adminDinas);
        task1.setSlaRecord(sla1);
        task1.setTaskStatus(TaskStatus.SEDANG_DIKERJAKAN);
        task1.setStartedAt(LocalDateTime.now().minusDays(1));
        fieldTaskRepository.save(task1);

        FieldTask task2 = new FieldTask();
        task2.setReport(report2);
        task2.setOfficer(petugas2);
        task2.setAssignedBy(adminDinas);
        task2.setSlaRecord(sla2);
        task2.setTaskStatus(TaskStatus.SELESAI);
        task2.setStartedAt(LocalDateTime.now().minusDays(25));
        task2.setCompletedAt(LocalDateTime.now().minusDays(20));
        fieldTaskRepository.save(task2);

        OfficerAttendance attendance1 = new OfficerAttendance();
        attendance1.setOfficer(petugas1);
        attendance1.setCheckInAt(LocalDateTime.now().minusHours(2));
        attendance1.setCheckInLatitude(new BigDecimal("3.58910000"));
        attendance1.setCheckInLongitude(new BigDecimal("98.67380000"));
        attendance1.setShiftStatus(ShiftStatus.AKTIF);
        attendance1.setDeviceInfo("Chrome/Windows");
        attendanceRepository.save(attendance1);

        System.out.println("=== Data Seeder: Initial data loaded successfully ===");
    }
}
