package com.example.authentification.service;

import com.example.authentification.entity.Utilisateur;
import com.example.authentification.repository.UtilisateurRepository;
import com.example.authentification.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.authentification.config.FirebaseInitializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.ListUsersPage;
import com.google.firebase.auth.SessionCookieOptions;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

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

    public void resetAttemptPostgres(String email) {
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

    private int extractIntClaim(Object value) {
        if (value == null)
            return 0;
        if (value instanceof Number)
            return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private void handleFailureFirebase(String email) throws Exception {
        int maxAttempts = parametreService.getIntValue("MAX_FAILED_ATTEMPS");
        try {
            UserRecord fbUser = FirebaseAuth.getInstance().getUserByEmail(email);
            Map<String, Object> claims = fbUser.getCustomClaims();
            claims = claims != null ? new HashMap<>(claims) : new HashMap<>();

            int attempts = extractIntClaim(claims.get("failed_attempts")) + 1;
            boolean blocked = attempts >= maxAttempts;

            claims.put("failed_attempts", attempts);
            claims.put("blocked", blocked);

            FirebaseAuth.getInstance().setCustomUserClaims(fbUser.getUid(), claims);
            System.out.println("Updated Firebase failed attempts for: " + email);
        } catch (Exception e) {
            throw new Exception("Could not update Firebase attempts: " + e.getMessage());
        }
    }

    public String loginWithFirebase(String email, String password) throws Exception {
        try {
            String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key="
                    + FIREBASE_API_KEY;

            RestTemplate restTemplate = new RestTemplate();

            String payload = String.format(
                    "{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}",
                    email, password);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response.getBody());
            String idToken = node.get("idToken").asText();

            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            Map<String, Object> claims = decodedToken.getClaims();

            boolean blocked = claims.get("blocked") instanceof Boolean
                    ? (Boolean) claims.get("blocked")
                    : false;

            if (blocked) {
                throw new RuntimeException("User account is blocked");
            }

            resetAttemptsFirebase(email);

            int sessionDurationMinutes = parametreService.getIntValue("SESSION_DURATION");
            String sessionCookie = FirebaseAuth.getInstance().createSessionCookie(
                    decodedToken.getUid(),
                    SessionCookieOptions.builder()
                            .setExpiresIn(sessionDurationMinutes * 60L * 1000L)
                            .build());

            return sessionCookie;

        } catch (Exception e) {
            handleFailureFirebase(email);
            throw new Exception(e.getMessage());
        }
    }

    public Utilisateur register(String email, String password, String nom) throws Exception {
        if(utilisateurRepository.findByEmail(email).isPresent()) {
            throw new Exception("Email already in use");
        }
        Utilisateur userEntity = new Utilisateur();
        userEntity.setId(UUID.randomUUID().toString());
        userEntity.setEmail(email);
        userEntity.setNom(nom);
        userEntity.setRole(10);
        userEntity.setBlocked(false);
        userEntity.setAttempts(0);
        userEntity.setPassword(password);

        return utilisateurRepository.save(userEntity);
    }

    public void syncFirebaseToPostgres() throws Exception {
        try {
            ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);

            while (page != null) {
                for (ExportedUserRecord fbUser : page.getValues()) {

                    Optional<Utilisateur> optionalPgUser = utilisateurRepository.findByEmail(fbUser.getEmail());

                    if (optionalPgUser.isPresent()) {
                        Utilisateur pgUser = optionalPgUser.get();

                        pgUser.setNom(fbUser.getDisplayName());
                        pgUser.setBlocked(fbUser.isDisabled());

                        Map<String, Object> claims = fbUser.getCustomClaims();
                        if (claims != null) {
                            if (claims.get("role") != null)
                                pgUser.setRole((Integer) claims.get("role"));

                            if (claims.get("attempts") != null)
                                pgUser.setAttempts((Integer) claims.get("attempts"));
                        }

                        utilisateurRepository.save(pgUser);
                    }
                }

                page = page.getNextPage();
            }

        } catch (Exception e) {
            System.out.println("Firebase → PostgreSQL sync failed: " + e.getMessage());
        }
    }

    public void syncPostgresToFirebase() throws Exception {
        try {
            List<Utilisateur> pgUsers = utilisateurRepository.findAll();

            for (Utilisateur pgUser : pgUsers) {

                UserRecord fbUser = null;

                try {
                    fbUser = FirebaseAuth.getInstance()
                            .getUserByEmail(pgUser.getEmail());
                } catch (Exception e) {
                    fbUser = null;
                }

                if (fbUser == null) {
                    CreateRequest request = new CreateRequest()
                            .setEmail(pgUser.getEmail())
                            .setDisplayName(pgUser.getNom())
                            .setPassword(pgUser.getPassword())
                            .setDisabled(pgUser.isBlocked());

                    fbUser = FirebaseAuth.getInstance().createUser(request);
                }

                Map<String, Object> claims = new HashMap<>();
                claims.put("role", pgUser.getRole());
                claims.put("attempts", pgUser.getAttempts());
                claims.put("blocked", pgUser.isBlocked());

                FirebaseAuth.getInstance().setCustomUserClaims(
                        fbUser.getUid(),
                        claims);
            }

        } catch (Exception e) {
            throw new Exception("PostgreSQL → Firebase sync failed: " + e.getMessage());
        }
    }
}