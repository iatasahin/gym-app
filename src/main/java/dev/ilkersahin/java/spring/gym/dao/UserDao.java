package dev.ilkersahin.java.spring.gym.dao;

import dev.ilkersahin.java.spring.gym.model.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDao {
    void persist(User user);
    User merge(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    List<User> getAll();
    Optional<User> deleteUser(String username);
}
