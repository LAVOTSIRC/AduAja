package com.plr.aduaja.repository;

import com.plr.aduaja.model.Region;
import com.plr.aduaja.model.Region.RegionLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionRepository extends JpaRepository<Region, String> {

    List<Region> findByRegionLevel(RegionLevel level);

    List<Region> findByParentRegionRegionId(String parentRegionId);

    Optional<Region> findByRegionNameAndRegionLevel(String regionName, RegionLevel level);

    List<Region> findByRegionNameContainingIgnoreCase(String regionName);
}
