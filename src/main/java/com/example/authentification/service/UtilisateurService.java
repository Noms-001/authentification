package com.example.authentification.service;

import com.example.authentification.entity.Utilisateur;
import com.example.authentification.repository.UtilisateurRepository;

import java.util.List;
import com.example.authentification.dto.UtilisateurDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository userRepository;

    @Autowired
    private SessionService sessionService;

    public Utilisateur updateUserByEmail(String email, String password, String nom) throws Exception {
        Utilisateur user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (password != null && !password.isBlank())
            user.setPassword(password);

        if (nom != null)
            user.setNom(nom);

        return userRepository.save(user);
    }

    public List<Utilisateur> searchUsers(String keyword, String authorizationHeader) throws Exception {
        UtilisateurDTO connectedUser = sessionService.getConnectedUser(authorizationHeader);
        if(connectedUser.getRole() < 20) {
            throw new Exception("Unauthorized: insufficient permissions");
        }
        List<Utilisateur> users;
        
        if (keyword != null && !keyword.isEmpty()) {
            users = userRepository.findByEmailContainingOrNomContaining(keyword);
        } else {
            users = userRepository.findAll();
        }
        
        return users;
    }

    public List<Utilisateur> getAllBlockedUsers() {
        return userRepository.findByIsBlockedTrue();
    }
}