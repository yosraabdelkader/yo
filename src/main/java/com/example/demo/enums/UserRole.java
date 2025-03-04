package com.example.demo.enums;

public enum UserRole {
    ADMIN, COVOITUREUR, CHAUFFEUR, TAXI;

    public static UserRole fromString(String role) {
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null; // Return null if the role is invalid
        }
    }
}
