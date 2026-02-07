package com.example.authentification.service;

import com.example.authentification.entity.Utilisateur;
import com.example.authentification.repository.UtilisateurRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private ParametreService parametreService;


    private void resetAttemptPostgres(String email) {
        Utilisateur user = utilisateurRepository.findByEmail(email).orElse(null);
        if (user != null) {
            user.setAttempts(0);
            user.setBlocked(false);
            utilisateurRepository.save(user);
        }
    }

}