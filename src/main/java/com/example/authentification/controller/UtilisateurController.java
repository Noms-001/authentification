package com.example.authentification.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.authentification.dto.UtilisateurDTO;
import com.example.authentification.entity.Utilisateur;
import com.example.authentification.service.UtilisateurService;

@RestController
@RequestMapping("/api/users")
public class UtilisateurController {
    @Autowired
    public UtilisateurService utilisateurService;

    @PutMapping("/{email}")
    public UtilisateurDTO updateUser(String email, String password, String nom) throws Exception {
        Utilisateur user = utilisateurService.updateUserByEmail(email, password, nom);
        return new UtilisateurDTO(user);
    }

    @
}
