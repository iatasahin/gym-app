package dev.ilkersahin.java.spring.gym.repository;

import dev.ilkersahin.java.spring.gym.model.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    // =========================================================================
    // SAVE TESTS (100s)
    // =========================================================================

    @Test
    @Order(101)
    void save_withValidUser_shouldPersistAndAssignId() {
        User user = new User("Jack", "Black", "Jack.Black", "password123", true);

        User saved = userRepository.save(user);

        assertThat(saved.getUserId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("Jack.Black");
    }

    // =========================================================================
    // FIND BY USERNAME TESTS (200s)
    // =========================================================================

    @Test
    @Order(201)
    void findByUsername_withExistingUser_shouldReturnUser() {
        User user = new User("Alice", "Smith", "Alice.Smith", "pass", true);
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("Alice.Smith");

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Alice");
    }

    @Test
    @Order(202)
    void findByUsername_withNonExistingUser_shouldReturnEmpty() {
        Optional<User> found = userRepository.findByUsername("NonExistent.User");

        assertThat(found).isEmpty();
    }

    // =========================================================================
    // EXISTS BY USERNAME TESTS (300s)
    // =========================================================================

    @Test
    @Order(301)
    void existsByUsername_withExistingUser_shouldReturnTrue() {
        User user = new User("Bob", "Jones", "Bob.Jones", "pass", true);
        userRepository.save(user);

        assertThat(userRepository.existsByUsername("Bob.Jones")).isTrue();
    }

    @Test
    @Order(302)
    void existsByUsername_withNonExistingUser_shouldReturnFalse() {
        assertThat(userRepository.existsByUsername("Nobody.Here")).isFalse();
    }
}
