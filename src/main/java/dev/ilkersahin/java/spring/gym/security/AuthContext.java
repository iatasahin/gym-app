package dev.ilkersahin.java.spring.gym.security;

public interface AuthContext {
    String getUsername();
    Role getRole();
    boolean isAuthenticated();
}
