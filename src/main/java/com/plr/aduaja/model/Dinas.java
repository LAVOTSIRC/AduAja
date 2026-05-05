package com.plr.aduaja.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dinas")
public class Dinas {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @ElementCollection
    @CollectionTable(name = "dinas_categories", joinColumns = @JoinColumn(name = "dinas_id"))
    @Column(name = "category")
    private List<String> categories = new ArrayList<>();

    @OneToMany(mappedBy = "dinas", cascade = CascadeType.ALL)
    private List<User> adminList = new ArrayList<>();

    @OneToMany(mappedBy = "targetDinas", cascade = CascadeType.ALL)
    private List<Disposisi> disposisiList = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }

    public List<User> getAdminList() { return adminList; }
    public void setAdminList(List<User> adminList) { this.adminList = adminList; }

    public List<Disposisi> getDisposisiList() { return disposisiList; }
    public void setDisposisiList(List<Disposisi> disposisiList) { this.disposisiList = disposisiList; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
