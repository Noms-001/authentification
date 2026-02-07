package com.example.authentification.dto;

import com.example.authentification.entity.Utilisateur;

public class UtilisateurDTO {
    private String id;
    private String email;
    private String nom;
    private Integer role;
    private Integer attemps;
    private Boolean blocked;

    public UtilisateurDTO(Utilisateur user) {
        this.id = user.getId(); 
        this.email = user.getEmail();
        this.nom = user.getNom();
        this.role = user.getRole();
        this.attemps = user.getAttempts();
        this.blocked = user.isBlocked();
    }

    public UtilisateurDTO(String id, String email, String nom, Integer role, Integer attemps, boolean blocked) {
        this.id = id;
        this.email = email;
        this.nom = nom;
        this.role = role;
        this.attemps = attemps;
        this.blocked = blocked;
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

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public Integer getAttemps() {
        return attemps;
    }

    public void setAttemps(Integer attemps) {
        this.attemps = attemps;
    }
}
