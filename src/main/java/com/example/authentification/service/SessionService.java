package com.example.authentification.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.authentification.dto.ApiResponse;
import com.example.authentification.dto.UtilisateurDTO;
import com.example.authentification.entity.Utilisateur;
import com.example.authentification.entity.Utilisateur;
import com.example.authentification.repository.UtilisateurRepository;
import com.example.authentification.util.JwtUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;

@Service
public class SessionService {
    private final UtilisateurRepository userRepository;
    private final JwtUtil jwtUtil;

    public SessionService(UtilisateurRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public UtilisateurDTO getConnectedUser(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Authorization header manquant ou invalide");
        }

        String token = authorizationHeader.substring(7);

        try {
            if (!jwtUtil.validateToken(token)) {
                throw new RuntimeException("Token local invalide ou expiré");
            }

            String userId = jwtUtil.getUserId(token);

            Utilisateur userEntity = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException(
                            "Utilisateur local non trouvé pour ID " + userId));

            UtilisateurDTO userResponse = new UtilisateurDTO(userEntity);

            return userResponse;

        } catch (Exception jwtEx) {
            throw new RuntimeException("Invalid token: " + jwtEx.getMessage());
        }
    }
}
