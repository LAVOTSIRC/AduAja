package com.plr.aduaja.model;

import jakarta.persistence.*;

@Entity
@Table(name = "regions")
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "region_id")
    private String regionId;

    @Column(name = "region_name", nullable = false)
    private String regionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "region_level", nullable = false)
    private RegionLevel regionLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_region_id")
    private Region parentRegion;

    @Column(name = "boundary_geojson", columnDefinition = "TEXT")
    private String boundaryGeojson;

    public enum RegionLevel {
        KOTA, KECAMATAN, KELURAHAN
    }

    public String getRegionId() { return regionId; }
    public void setRegionId(String regionId) { this.regionId = regionId; }

    public String getRegionName() { return regionName; }
    public void setRegionName(String regionName) { this.regionName = regionName; }

    public RegionLevel getRegionLevel() { return regionLevel; }
    public void setRegionLevel(RegionLevel regionLevel) { this.regionLevel = regionLevel; }

    public Region getParentRegion() { return parentRegion; }
    public void setParentRegion(Region parentRegion) { this.parentRegion = parentRegion; }

    public String getBoundaryGeojson() { return boundaryGeojson; }
    public void setBoundaryGeojson(String boundaryGeojson) { this.boundaryGeojson = boundaryGeojson; }
}
