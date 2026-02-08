package com.example.authentification.controller;

import com.example.authentification.dto.ApiResponse;
import com.example.authentification.dto.UtilisateurDTO;
import com.example.authentification.entity.Utilisateur;
import com.example.authentification.service.AuthService;

import java.util.HashMap;
import java.util.Map;

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

    @PutMapping("/register")
    public ResponseEntity<ApiResponse<UtilisateurDTO>> register(@RequestParam String email, @RequestParam String nom, @RequestParam String password, @RequestParam(required = false) Integer role) {
        try {
            Utilisateur user = authService.register(email, password, nom, role);
            UtilisateurDTO dto = new UtilisateurDTO(user);
            return ResponseEntity.ok(ApiResponse.success(dto));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> syncFirebase() {
        try {
            int firebaseSynced = authService.syncFirebaseToPostgres();
            int postgresSynced = authService.syncPostgresToFirebase();
            Map<String, Integer> result = new HashMap<>();
            result.put("from_firebase", firebaseSynced);
            result.put("from_postgres", postgresSynced);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch(Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/user/{email}/unblock")
    public ResponseEntity<ApiResponse<String>> unblockByEmail(@PathVariable String email) {
        try {
            authService.resetAttemptPostgres(email);
            return ResponseEntity.ok(new ApiResponse<>(true, "User unblocked successfully", null));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, null, e.getMessage()));
        }
    }
}
