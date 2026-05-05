package com.plr.aduaja.service;

import com.plr.aduaja.model.Laporan;
import com.plr.aduaja.repository.LaporanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LaporanService {

    @Autowired // Memanggil mesin repository agar bisa digunakan di sini
    private LaporanRepository laporanRepository;

    // Fungsi untuk menyimpan laporan baru ke database
    public void simpanLaporanBaru(Laporan laporan) {
        // Nanti logika tambahan seperti ngecek jam kerja atau batas wilayah bisa ditaruh di sini
        laporanRepository.save(laporan);
    }
}