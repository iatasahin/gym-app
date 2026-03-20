package dev.ilkersahin.java.spring.gym.workload.security;

public enum Role {
    TRAINEE, TRAINER;

    public static Role fromString(String value) {
        if (value == null) return null;
        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        return null;
    }
}
