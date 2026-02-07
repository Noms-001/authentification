package com.example.authentification.controller;

import com.example.authentification.dto.ApiResponse;
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
}
