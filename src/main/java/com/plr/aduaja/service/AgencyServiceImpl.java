package com.plr.aduaja.service;

import com.plr.aduaja.model.Agency;
import com.plr.aduaja.repository.AgencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AgencyServiceImpl implements AgencyService {

    @Autowired
    private AgencyRepository agencyRepository;

    // ===================================================
    // OVERLOADING: getAgencies() — Compile-time Polymorphism
    // Setiap versi memiliki parameter berbeda, nama sama
    // ===================================================

    @Override
    public List<Agency> getAgencies() {  // OVERLOAD 1: tanpa parameter
        return agencyRepository.findAll();
    }

    @Override
    public List<Agency> getAgencies(boolean activeOnly) {  // OVERLOAD 2: filter aktif
        return activeOnly ? agencyRepository.findByIsActiveTrue() : agencyRepository.findAll();
    }

    @Override
    public List<Agency> getAgencies(String regionId) {  // OVERLOAD 3: filter region
        return agencyRepository.findByRegionRegionId(regionId);
    }

    @Override
    public List<Agency> getAgencies(String regionId, boolean activeOnly) {  // OVERLOAD 4: region + aktif
        return activeOnly
                ? agencyRepository.findByIsActiveTrueAndRegionRegionId(regionId)
                : agencyRepository.findByRegionRegionId(regionId);
    }

    // ===================================================
    // Alias lama — backward compatibility
    // ===================================================

    @Override
    public List<Agency> getAllAgencies() {
        return getAgencies();  // delegate ke overload
    }

    @Override
    public List<Agency> getActiveAgencies() {
        return getAgencies(true);  // delegate ke overload
    }

    @Override
    public Optional<Agency> getAgencyById(String id) {
        return agencyRepository.findById(id);
    }

    @Override
    public List<Agency> getAgenciesByRegion(String regionId) {
        return getAgencies(regionId);  // delegate ke overload
    }

    @Override
    public List<Agency> getActiveAgenciesByRegion(String regionId) {
        return getAgencies(regionId, true);  // delegate ke overload
    }

    @Override
    public Agency createAgency(Agency agency) {
        return agencyRepository.save(agency);
    }

    @Override
    public Agency updateAgency(Agency agency) {
        return agencyRepository.save(agency);
    }
}
