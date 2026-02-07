package com.example.authentification.repository;

import com.example.authentification.entity.Parametre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParametreRepository extends JpaRepository<Parametre, String> {

    Optional<Parametre> findByCle(String cle);
}
