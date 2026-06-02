package com.plr.aduaja.repository;

import com.plr.aduaja.model.Agency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgencyRepository extends JpaRepository<Agency, String> {

    List<Agency> findByIsActiveTrue();

    List<Agency> findByRegionRegionId(String regionId);

    Optional<Agency> findByAgencyName(String agencyName);

    List<Agency> findByIsActiveTrueAndRegionRegionId(String regionId);
}
