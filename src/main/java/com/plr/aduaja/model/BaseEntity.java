package com.plr.aduaja.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// ============================================================
// INHERITANCE (Pewarisan) — Parent class untuk semua Entity
// Semua entity di sistem ini extends BaseEntity
// ============================================================
@MappedSuperclass
public abstract class BaseEntity {

    // ENKAPSULASI: field private, hanya bisa diakses melalui getter
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // INHERITANCE: method lifecycle otomatis
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ENKAPSULASI: getter + setter untuk backward compatibility
    // (beberapa service lama masih memanggil setUpdatedAt secara manual)
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
