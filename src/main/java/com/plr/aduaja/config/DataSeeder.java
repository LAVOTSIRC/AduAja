package com.plr.aduaja.config;

import com.plr.aduaja.model.*;
import com.plr.aduaja.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DinasRepository dinasRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        Dinas dinasPU = new Dinas();
        dinasPU.setCode("DINAS_PU");
        dinasPU.setName("Dinas PU dan Penataan Ruang");
        dinasPU.setCategories(List.of("Infrastruktur Jalan", "Drainase", "Trotoar", "Jembatan"));
        dinasRepository.save(dinasPU);

        Dinas dinasLH = new Dinas();
        dinasLH.setCode("DINAS_LH");
        dinasLH.setName("Dinas Lingkungan Hidup");
        dinasLH.setCategories(List.of("Lingkungan Hidup", "Pencemaran", "Sampah"));
        dinasRepository.save(dinasLH);

        Dinas dinasESDM = new Dinas();
        dinasESDM.setCode("DINAS_ESDM");
        dinasESDM.setName("Dinas ESDM");
        dinasESDM.setCategories(List.of("Penerangan Jalan", "Listrik Publik"));
        dinasRepository.save(dinasESDM);

        User adminPusat = new User();
        adminPusat.setUsername("admin_pusat");
        adminPusat.setPassword(passwordEncoder.encode("admin123"));
        adminPusat.setName("Admin Pusat");
        adminPusat.setEmail("admin@aduaja.go.id");
        adminPusat.setRole(User.Role.ADMIN_PUSAT);
        adminPusat.setIsActive(true);
        userRepository.save(adminPusat);

        User adminDinas = new User();
        adminDinas.setUsername("admin_dinas");
        adminDinas.setPassword(passwordEncoder.encode("admin123"));
        adminDinas.setName("Admin Dinas PU");
        adminDinas.setEmail("admin.pu@aduaja.go.id");
        adminDinas.setRole(User.Role.ADMIN_DINAS);
        adminDinas.setDinas(dinasPU);
        adminDinas.setIsActive(true);
        userRepository.save(adminDinas);

        User petugas1 = new User();
        petugas1.setUsername("petugas1");
        petugas1.setPassword(passwordEncoder.encode("petugas123"));
        petugas1.setName("Ahmad Fauzi");
        petugas1.setEmail("ahmad.fauzi@aduaja.go.id");
        petugas1.setPhoneNumber("081234567890");
        petugas1.setRole(User.Role.PETUGAS);
        petugas1.setDinas(dinasPU);
        petugas1.setIsActive(true);
        userRepository.save(petugas1);

        User petugas2 = new User();
        petugas2.setUsername("petugas2");
        petugas2.setPassword(passwordEncoder.encode("petugas123"));
        petugas2.setName("Rizal Harahap");
        petugas2.setEmail("rizal.harahap@aduaja.go.id");
        petugas2.setPhoneNumber("082345678901");
        petugas2.setRole(User.Role.PETUGAS);
        petugas2.setDinas(dinasPU);
        petugas2.setIsActive(true);
        userRepository.save(petugas2);

        User warga1 = new User();
        warga1.setUsername("budi_santoso");
        warga1.setPassword(passwordEncoder.encode("warga123"));
        warga1.setName("Budi Santoso");
        warga1.setEmail("budi.santoso@email.com");
        warga1.setPhoneNumber("081234567890");
        warga1.setNik("1271052504900001");
        warga1.setRole(User.Role.WARGA);
        warga1.setAddress("Jl. Setia Budi No. 12");
        warga1.setKelurahan("Babura");
        warga1.setKecamatan("Medan Baru");
        warga1.setKota("Kota Medan");
        warga1.setProvinsi("Sumatera Utara");
        warga1.setIsActive(true);
        userRepository.save(warga1);

        User warga2 = new User();
        warga2.setUsername("sari_dewi");
        warga2.setPassword(passwordEncoder.encode("warga123"));
        warga2.setName("Sari Dewi");
        warga2.setEmail("sari.dewi@email.com");
        warga2.setPhoneNumber("085678901234");
        warga2.setRole(User.Role.WARGA);
        warga2.setAddress("Gang Melati No. 5");
        warga2.setKecamatan("Medan Baru");
        warga2.setKota("Kota Medan");
        warga2.setIsActive(true);
        userRepository.save(warga2);

        Report report1 = new Report();
        report1.setTitle("Jalan Berlubang di Jl. Sudirman");
        report1.setDescription("Jalan berlubang besar diameter ±50cm, kedalaman ±15cm. Sudah terjadi sejak 2 minggu lalu dan semakin membesar.");
        report1.setCategory("Infrastruktur Jalan");
        report1.setStatus(Report.Status.DIPROSES);
        report1.setReporter(warga1);
        report1.setLocation("Jl. Sudirman, Medan Kota");
        report1.setLatitude("3.5891");
        report1.setLongitude("98.6738");
        report1.setLandmark("Dekat Indomaret Sudirman");
        report1.setCreatedAt(LocalDateTime.now().minusDays(14));
        report1.setSlaDeadline(LocalDateTime.now().plusDays(3));
        reportRepository.save(report1);

        Report report2 = new Report();
        report2.setTitle("Lampu Jalan Mati di Gang Melati");
        report2.setDescription("Tiang lampu nomor 3 dari ujung jalan tidak menyala sejak 1 bulan terakhir.");
        report2.setCategory("Listrik Publik");
        report2.setStatus(Report.Status.SELESAI);
        report2.setReporter(warga2);
        report2.setLocation("Gang Melati, Medan Baru");
        report2.setLatitude("3.5823");
        report2.setLongitude("98.6701");
        report2.setLandmark("Seberang Musholla Al-Ikhlas");
        report2.setCreatedAt(LocalDateTime.now().minusDays(30));
        report2.setSlaDeadline(LocalDateTime.now().minusDays(5));
        reportRepository.save(report2);

        Report report3 = new Report();
        report3.setTitle("Saluran Drainase Tersumbat");
        report3.setDescription("Drainase tersumbat sampah dan lumpur, menyebabkan genangan air saat hujan deras.");
        report3.setCategory("Sistem Drainase");
        report3.setStatus(Report.Status.MENUNGGU);
        report3.setReporter(warga1);
        report3.setLocation("Jl. Gatot Subroto, Medan Petisah");
        report3.setLatitude("3.5912");
        report3.setLongitude("98.6645");
        report3.setLandmark("Depan Kantor Pos");
        report3.setCreatedAt(LocalDateTime.now().minusDays(2));
        report3.setSlaDeadline(LocalDateTime.now().plusDays(12));
        reportRepository.save(report3);

        Ticket ticket1 = new Ticket();
        ticket1.setTicketNumber("TKT-2025-001");
        ticket1.setReport(report1);
        ticket1.setAssignedPetugas(petugas1);
        ticket1.setAssignedByDinas(adminDinas);
        ticket1.setStatus(Ticket.Status.IN_PROGRESS);
        ticket1.setPriority(Ticket.Priority.TINGGI);
        ticket1.setLocation("Jl. Sudirman No. 10, Medan Kota");
        ticket1.setLatitude("3.5891");
        ticket1.setLongitude("98.6738");
        ticket1.setSlaDeadline(LocalDateTime.now().plusDays(3));
        ticket1.setStartedAt(LocalDateTime.now().minusDays(1));
        ticket1.setCreatedAt(LocalDateTime.now().minusDays(2));
        ticketRepository.save(ticket1);

        Ticket ticket2 = new Ticket();
        ticket2.setTicketNumber("TKT-2025-002");
        ticket2.setReport(report2);
        ticket2.setAssignedPetugas(petugas2);
        ticket2.setAssignedByDinas(adminDinas);
        ticket2.setStatus(Ticket.Status.SELESAI);
        ticket2.setPriority(Ticket.Priority.SEDANG);
        ticket2.setLocation("Gang Melati No. 5, Medan Baru");
        ticket2.setLatitude("3.5823");
        ticket2.setLongitude("98.6701");
        ticket2.setSlaDeadline(LocalDateTime.now().minusDays(5));
        ticket2.setStartedAt(LocalDateTime.now().minusDays(25));
        ticket2.setCompletedAt(LocalDateTime.now().minusDays(20));
        ticket2.setCreatedAt(LocalDateTime.now().minusDays(30));
        ticketRepository.save(ticket2);

        System.out.println("=== Data Seeder: Initial data loaded successfully ===");
    }
}
