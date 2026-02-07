package com.example.authentification.controller;

import com.example.authentification.dto.ApiResponse;
import com.example.authentification.dto.UtilisateurDTO;
import com.example.authentification.entity.Utilisateur;
import com.example.authentification.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("postgres/login")
    public ResponseEntity<ApiResponse<String>> loginWithPostgres(@RequestParam String email, @RequestParam String password) {
        try {
            String token = authService.loginWithPostgres(email, password);
            ApiResponse<String> response = ApiResponse.success(token);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<String> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("firebase/login")
    public ResponseEntity<ApiResponse<String>> loginWithFirebase(@RequestParam String email, @RequestParam String password) {
        try {
            String token = authService.loginWithFirebase(email, password);
            ApiResponse<String> response = ApiResponse.success(token);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<String> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UtilisateurDTO>> register(@RequestParam String email, @RequestParam String nom, @RequestParam String password) {
        try {
            Utilisateur user = authService.register(email, password, nom);
            UtilisateurDTO dto = new UtilisateurDTO(user.getId(), user.getEmail(), user.getNom(), user.getRole());
            return ResponseEntity.ok(ApiResponse.success(dto));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
}
