package com.plr.aduaja.controller; // Pastikan nama package-nya benar!

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // WAJIB ADA! Ini yang ngasih tahu Java kalau class ini bertugas jadi Penunjuk Arah
public class WebController {

    @GetMapping("/") // Rute untuk URL utama (localhost:8080/)
    public String tampilkanIndex() {
        return "index"; // Java akan langsung mencari file HTML bernama 'beranda.html'
    }
}