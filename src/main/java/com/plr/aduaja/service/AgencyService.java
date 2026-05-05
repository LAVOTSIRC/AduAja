package com.plr.aduaja.service;

import com.plr.aduaja.model.Agency;
import com.plr.aduaja.repository.AgencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AgencyService {

    @Autowired
    private AgencyRepository agencyRepository;

    public List<Agency> getAllAgencies() {
        return agencyRepository.findAll();
    }

    public List<Agency> getActiveAgencies() {
        return agencyRepository.findByIsActiveTrue();
    }

    public Optional<Agency> getAgencyById(String id) {
        return agencyRepository.findById(id);
    }

    public List<Agency> getAgenciesByRegion(String regionId) {
        return agencyRepository.findByRegionRegionId(regionId);
    }

    public List<Agency> getActiveAgenciesByRegion(String regionId) {
        return agencyRepository.findByIsActiveTrueAndRegionRegionId(regionId);
    }

    public Agency createAgency(Agency agency) {
        return agencyRepository.save(agency);
    }

    public Agency updateAgency(Agency agency) {
        return agencyRepository.save(agency);
    }
}
