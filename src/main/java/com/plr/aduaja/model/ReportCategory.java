package com.plr.aduaja.model;

import jakarta.persistence.*;

@Entity
@Table(name = "report_categories")
public class ReportCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "category_id")
    private String categoryId;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(name = "sla_duration_hours", nullable = false)
    private Integer slaDurationHours;

    @Column(columnDefinition = "TEXT")
    private String description;

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Integer getSlaDurationHours() { return slaDurationHours; }
    public void setSlaDurationHours(Integer slaDurationHours) { this.slaDurationHours = slaDurationHours; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
