package com.plr.aduaja.repository;

import com.plr.aduaja.model.Dinas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DinasRepository extends JpaRepository<Dinas, String> {

    Optional<Dinas> findByCode(String code);

    Optional<Dinas> findByName(String name);

    boolean existsByCode(String code);

    boolean existsByName(String name);

    @SuppressWarnings("unchecked")
    List<Dinas> findByCategoriesContaining(String category);
}
