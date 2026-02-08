package com.example.authentification.repository;

import com.example.authentification.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, String> {
    Optional<Utilisateur> findByEmail(String email);
    
    List<Utilisateur> findByIsBlockedTrue();

    @Query("SELECT u FROM Utilisateur u WHERE u.email LIKE %:keyword% OR u.nom LIKE %:keyword%")
    List<Utilisateur> findByEmailContainingOrNomContaining(@Param("keyword") String keyword);
    
}