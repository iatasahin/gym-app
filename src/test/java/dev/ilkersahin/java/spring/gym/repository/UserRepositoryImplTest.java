package dev.ilkersahin.java.spring.gym.repository;

import dev.ilkersahin.java.spring.gym.config.TestPersistenceConfig;
import dev.ilkersahin.java.spring.gym.dao.UserDao;
import dev.ilkersahin.java.spring.gym.model.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestPersistenceConfig.class)
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRepositoryImplTest {
    @Autowired
    private UserDao userRepository; ;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Jack", "Black", "Jack.Black", "password123", true);
    }

    // =========================================================================
    // PERSIST TESTS (100s)
    // =========================================================================

    @Test
    @Order(101)
    void persist_withValidUser_shouldSaveUser() {
        userRepository.persist(user);

        Optional<User> found = userRepository.findByUsername("Jack.Black");

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Jack");
    }

    @Test
    @Order(102)
    void persist_withValidUser_shouldAssignId() {
        userRepository.persist(user);

        assertThat(user.getUserId()).isNotNull();
    }

    // =========================================================================
    // MERGE TESTS (200s)
    // =========================================================================

    @Test
    @Order(201)
    void merge_withExistingUser_shouldUpdateUser() {
        userRepository.persist(user);

        user.setFirstName("John");
        User merged = userRepository.merge(user);

        assertThat(merged.getFirstName()).isEqualTo("John");
    }

    // =========================================================================
    // FIND BY ID TESTS (300s)
    // =========================================================================

    @Test
    @Order(301)
    void findById_withExistingId_shouldReturnUser() {
        userRepository.persist(user);

        Optional<User> found = userRepository.findById(user.getUserId());

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("Jack.Black");
    }

    @Test
    @Order(302)
    void findById_withNonExistingId_shouldReturnEmpty() {
        Optional<User> found = userRepository.findById(UUID.randomUUID());

        assertThat(found).isEmpty();
    }

    // =========================================================================
    // FIND BY USERNAME TESTS (400s)
    // =========================================================================

    @Test
    @Order(401)
    void findByUsername_withExistingUsername_shouldReturnUser() {
        userRepository.persist(user);

        Optional<User> found = userRepository.findByUsername("Jack.Black");

        assertThat(found).isPresent();
    }

    @Test
    @Order(402)
    void findByUsername_withNonExistingUsername_shouldReturnEmpty() {
        Optional<User> found = userRepository.findByUsername("Non.Existent");

        assertThat(found).isEmpty();
    }

    // =========================================================================
    // EXISTS BY USERNAME TESTS (500s)
    // =========================================================================

    @Test
    @Order(501)
    void existsByUsername_withExistingUsername_shouldReturnTrue() {
        userRepository.persist(user);

        assertThat(userRepository.existsByUsername("Jack.Black")).isTrue();
    }

    @Test
    @Order(502)
    void existsByUsername_withNonExistingUsername_shouldReturnFalse() {
        assertThat(userRepository.existsByUsername("Non.Existent")).isFalse();
    }

    // =========================================================================
    // GET ALL TESTS (600s)
    // =========================================================================

    @Test
    @Order(601)
    void getAll_withNoUsers_shouldReturnEmptyList() {
        List<User> users = userRepository.getAll();

        assertThat(users).isEmpty();
    }

    @Test
    @Order(602)
    void getAll_withMultipleUsers_shouldReturnAllUsersOrdered() {
        userRepository.persist(new User("Bob", "B", "Bob.B", "p", true));
        userRepository.persist(new User("Alice", "A", "Alice.A", "p", true));

        List<User> users = userRepository.getAll();

        assertThat(users).hasSize(2);
        assertThat(users.get(0).getUsername()).isEqualTo("Alice.A");
        assertThat(users.get(1).getUsername()).isEqualTo("Bob.B");
    }
}
