package com.plr.aduaja.service;

import com.plr.aduaja.model.Agency;

import java.util.List;
import java.util.Optional;

public interface AgencyService {

    // OVERLOADING: 3 versi getAgencies dengan parameter berbeda (Compile-time Polymorphism)
    List<Agency> getAgencies();

    List<Agency> getAgencies(boolean activeOnly);  // OVERLOAD

    List<Agency> getAgencies(String regionId);  // OVERLOAD

    List<Agency> getAgencies(String regionId, boolean activeOnly);  // OVERLOAD

    // Alias lama — backward compatibility
    List<Agency> getAllAgencies();

    List<Agency> getActiveAgencies();

    Optional<Agency> getAgencyById(String id);

    List<Agency> getAgenciesByRegion(String regionId);

    List<Agency> getActiveAgenciesByRegion(String regionId);

    Agency createAgency(Agency agency);

    Agency updateAgency(Agency agency);
}
