package com.plr.aduaja.model;

import jakarta.persistence.*;

// ============================================================
// INHERITANCE (Pewarisan): ReportCategory extends BaseEntity
// Mendapatkan createdAt dan updatedAt otomatis dari parent
// ============================================================
@Entity
@Table(name = "report_categories")
public class ReportCategory extends BaseEntity {  // ← INHERITANCE sejati

    // ENKAPSULASI: semua field PRIVATE
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "category_id")
    private String categoryId;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(name = "sla_duration_hours", nullable = false)
    private Integer slaDurationHours = 72;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_name", length = 50)
    private String iconName;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // ENKAPSULASI: Hanya getter & setter
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    // alias "name" for compatibility with prompt templates
    public String getName() { return categoryName; }
    public void setName(String name) { this.categoryName = name; }

    public Integer getSlaDurationHours() { return slaDurationHours; }
    public void setSlaDurationHours(Integer slaDurationHours) { this.slaDurationHours = slaDurationHours; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
