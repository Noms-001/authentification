package com.example.authentification.dto;

public class UtilisateurDTO {
    private String id;
    private String email;
    private String nom;
    private Integer role;

    public UtilisateurDTO(String id, String email, String nom, Integer role) {
        this.id = id;
        this.email = email;
        this.nom = nom;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNom() {
        return nom;
    }
    public Integer getRole() {
        return role;
    }
}
