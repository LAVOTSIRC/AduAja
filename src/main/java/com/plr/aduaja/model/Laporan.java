package com.plr.aduaja.model;

import jakarta.persistence.*;
import lombok.Data;

@Data // Sihir Lombok: otomatis bikin Getter & Setter
@Entity // Memberitahu JPA: "Jadikan Class ini sebagai tabel di database"
@Table(name = "tabel_laporan")
public class Laporan {

    @Id // Ini jadi Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto Increment (1, 2, 3...)
    private Long id;

    @Column(nullable = false)
    private String judul;

    @Column(nullable = false, length = 1000)
    private String deskripsi;

    // Nanti atribut lain seperti lokasi, foto, dll bisa ditambahkan di sini
}