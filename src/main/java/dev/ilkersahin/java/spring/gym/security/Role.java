package dev.ilkersahin.java.spring.gym.security;

import dev.ilkersahin.java.spring.gym.exception.RoleDoesNotExistException;

public enum Role {
    TRAINEE("Trainee"),
    TRAINER("Trainer");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Parse role from string (case-insensitive).
     * Accepts both enum name ("TRAINEE") and display name ("Trainee").
     *
     * @param value the string value to parse
     * @return the matching Role, or null if value is null
     * @throws IllegalArgumentException if value doesn't match any role
     */
    public static Role fromString(String value) {
        if (value == null) {
            return null;
        }
        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(value) ||
                    role.displayName.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new RoleDoesNotExistException("Unknown role: " + value);
    }
}
