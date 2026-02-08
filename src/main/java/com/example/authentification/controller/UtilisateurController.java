package com.example.authentification.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.stream.Collectors;
import com.example.authentification.dto.ApiResponse;

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

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UtilisateurDTO>>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            List<Utilisateur> users = utilisateurService.searchUsers(keyword, authorizationHeader);
            List<UtilisateurDTO> userDTOs = users.stream().map(UtilisateurDTO::new).collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, userDTOs, null));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, null, e.getMessage()));
        }
    }
}
