package com.plr.aduaja.dto;

// ============================================================
// ENCAPSULATION (Enkapsulasi): DTO memisahkan filter form dari Entity
// Semua field PRIVATE — Controller tidak langsung pakai Entity
// ============================================================
public class ReportFilterDTO {

    // ENKAPSULASI: semua field PRIVATE
    private String status;
    private String searchQuery;
    private String categoryId;
    private String startDate;
    private String endDate;

    // ENKAPSULASI: Hanya getter & setter
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}
