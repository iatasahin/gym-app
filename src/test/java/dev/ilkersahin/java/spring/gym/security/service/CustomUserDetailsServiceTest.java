package dev.ilkersahin.java.spring.gym.security.service;

import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import dev.ilkersahin.java.spring.gym.model.User;
import dev.ilkersahin.java.spring.gym.repository.UserRepository;
import dev.ilkersahin.java.spring.gym.security.Role;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomUserDetailsServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User traineeUser;
    private User trainerUser;

    @BeforeEach
    void setUp() {
        // Trainee user
        traineeUser = new User("John", "Doe", "john.doe", "password123", true);
        Trainee trainee = new Trainee();
        trainee.setUser(traineeUser);
        // Set bidirectional relationship
        traineeUser.setTrainee(trainee);

        // Trainer user
        trainerUser = new User("Jane", "Smith", "jane.smith", "password456", true);
        Trainer trainer = new Trainer();
        trainer.setUser(trainerUser);
        trainer.setSpecialization(TrainingType.fromEnum(TrainingType.Type.FITNESS));
        trainerUser.setTrainer(trainer);
    }

    // =========================================================================
    // loadUserByUsername TESTS (100s)
    // =========================================================================

    @Test
    @Order(101)
    void loadUserByUsername_withTrainee_shouldReturnUserDetailsWithTraineeRole() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(traineeUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("john.doe");

        assertThat(userDetails.getUsername()).isEqualTo("john.doe");
        assertThat(userDetails.getPassword()).isEqualTo("password123");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_TRAINEE");
    }

    @Test
    @Order(102)
    void loadUserByUsername_withTrainer_shouldReturnUserDetailsWithTrainerRole() {
        when(userRepository.findByUsername("jane.smith")).thenReturn(Optional.of(trainerUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("jane.smith");

        assertThat(userDetails.getUsername()).isEqualTo("jane.smith");
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_TRAINER");
    }

    @Test
    @Order(103)
    void loadUserByUsername_withInactiveUser_shouldReturnDisabledUserDetails() {
        traineeUser.setActive(false);
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(traineeUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("john.doe");

        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    @Order(104)
    void loadUserByUsername_withNonExistentUser_shouldThrowUsernameNotFoundException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nonexistent"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("nonexistent");
    }

    // =========================================================================
    // getPrimaryRole TESTS (200s)
    // =========================================================================

    @Test
    @Order(201)
    void getPrimaryRole_withTrainee_shouldReturnTrainee() {
        Role role = userDetailsService.getPrimaryRole(traineeUser);

        assertThat(role).isEqualTo(Role.TRAINEE);
    }

    @Test
    @Order(202)
    void getPrimaryRole_withTrainer_shouldReturnTrainer() {
        Role role = userDetailsService.getPrimaryRole(trainerUser);

        assertThat(role).isEqualTo(Role.TRAINER);
    }

    @Test
    @Order(203)
    void getPrimaryRole_withNoRole_shouldThrowException() {
        User noRoleUser = new User("No", "Role", "no.role", "password", true);

        assertThatThrownBy(() -> userDetailsService.getPrimaryRole(noRoleUser))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no.role");
    }
}
