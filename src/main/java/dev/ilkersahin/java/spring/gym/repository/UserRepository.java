package dev.ilkersahin.java.spring.gym.repository;

import dev.ilkersahin.java.spring.gym.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by username.
     * Derived query - Spring Data generates: SELECT u FROM User u WHERE u.username = ?1
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if username exists.
     * Derived query - Spring Data generates: SELECT COUNT(u) > 0 FROM User u WHERE u.username = ?1
     */
    boolean existsByUsername(String username);

    /**
     * Get all users ordered by username.
     * Replaces named query "User.getAll"
     */
    List<User> findAllByOrderByUsername();
}
