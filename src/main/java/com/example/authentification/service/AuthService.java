package com.example.authentification.service;

import com.example.authentification.entity.Utilisateur;
import com.example.authentification.repository.UtilisateurRepository;
import com.example.authentification.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.authentification.config.FirebaseInitializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.SessionCookieOptions;
import com.google.firebase.auth.UserRecord;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private static final String FIREBASE_API_KEY = "AIzaSyCWwCxN_mJ35ClzQ_3I9LFEbH5-vdkiI3Q";

    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private ParametreService parametreService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostConstruct
    public void init() throws Exception {
        FirebaseInitializer.getFirebaseApp();
    }


    public String loginWithPostgres(String email, String password) throws Exception {
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isBlocked()) {
            throw new RuntimeException("User account is blocked");
        }

        if (!password.equals(user.getPassword())) {
            int leftAttemps = handleFailurePostgres(email);
            throw new RuntimeException("Wrong password only " + leftAttemps + " attemps left.");
        }

        user.setAttempts(0);
        utilisateurRepository.save(user);
        resetAttemptPostgres(email);

        String token = jwtUtil.generateToken(user.getId(), parametreService.getIntValue("SESSION_DURATION"));

        return token;
    }

    private void resetAttemptPostgres(String email) {
        Utilisateur user = utilisateurRepository.findByEmail(email).orElse(null);
        if (user != null) {
            user.setAttempts(0);
            user.setBlocked(false);
            utilisateurRepository.save(user);
        }
    }

    private int handleFailurePostgres(String email) throws Exception {
        try {
            int maxAttempts = parametreService.getIntValue("MAX_FAILED_ATTEMPTS");
            Utilisateur pgUser = utilisateurRepository.findByEmail(email).orElse(null);
            if (pgUser != null) {
                int attempts = pgUser.getAttempts() + 1;
                pgUser.setAttempts(attempts);
                pgUser.setBlocked(attempts >= maxAttempts);
                utilisateurRepository.save(pgUser);
                return Math.max(maxAttempts - attempts, 0);
            }
            return 0;

        } catch (Exception e) {
            throw new Exception("Failed to update failed attempts: " + e.getMessage());
        }
    }

    private void resetAttemptsFirebase(String email) throws Exception {
        try {
            UserRecord fbUser = FirebaseAuth.getInstance().getUserByEmail(email);

            Map<String, Object> claims = fbUser.getCustomClaims();
            claims = claims != null ? new HashMap<>(claims) : new HashMap<>();

            claims.put("failed_attempts", 0);
            claims.put("blocked", false);

            FirebaseAuth.getInstance().setCustomUserClaims(fbUser.getUid(), claims);

        } catch (Exception e) {
            throw new Exception("Failed to reset attempts: " + e.getMessage());
        }
    }

}