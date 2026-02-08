package com.example.authentification.service;

import com.example.authentification.entity.Utilisateur;
import com.example.authentification.repository.UtilisateurRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository userRepository;

    public Utilisateur updateUserByEmail(String email, String password, String nom) throws Exception {
        Utilisateur user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (password != null && !password.isBlank())
            user.setPassword(password);

        if (nom != null)
            user.setNom(nom);

        return userRepository.save(user);
    }
}