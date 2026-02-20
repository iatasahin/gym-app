package dev.ilkersahin.java.spring.gym.security.service;

import dev.ilkersahin.java.spring.gym.exception.AccountLockedException;
import dev.ilkersahin.java.spring.gym.model.util.LoginAttempt;
import dev.ilkersahin.java.spring.gym.repository.util.LoginAttemptRepository;
import dev.ilkersahin.java.spring.gym.security.config.BruteForceProtectionProperties;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginAttemptServiceTest {

    @Mock private LoginAttemptRepository loginAttemptRepository;
    @Mock private BruteForceProtectionProperties properties;

    @InjectMocks
    private LoginAttemptService loginAttemptService;

    private static final String TEST_USERNAME = "test.user";
    private static final String TEST_IP = "192.168.1.1";

    @BeforeEach
    void setUp() {
        // Default: brute force protection enabled
        when(properties.isEnabled()).thenReturn(true);
        lenient().when(properties.getMaxAttempts()).thenReturn(3);
        lenient().when(properties.getBlockDurationMinutes()).thenReturn(5);
    }


    // =========================================================================
    // checkIfBlocked TESTS (100s)
    // =========================================================================

    @Test
    @Order(101)
    void checkIfBlocked_whenBelowMaxAttempts_shouldNotThrow() {
        when(loginAttemptRepository.countRecentAttempts(eq(TEST_USERNAME), any(Instant.class)))
                .thenReturn(2);

        // Should not throw
        loginAttemptService.checkIfBlocked(TEST_USERNAME);
    }

    @Test
    @Order(102)
    void checkIfBlocked_whenAtMaxAttempts_shouldThrowAccountLockedException() {
        when(loginAttemptRepository.countRecentAttempts(eq(TEST_USERNAME), any(Instant.class)))
                .thenReturn(3);
        when(loginAttemptRepository.findMostRecentAttempt(TEST_USERNAME))
                .thenReturn(Optional.of(new LoginAttempt(TEST_USERNAME, Instant.now())));

        assertThatThrownBy(() -> loginAttemptService.checkIfBlocked(TEST_USERNAME))
                .isInstanceOf(AccountLockedException.class);
    }

    @Test
    @Order(103)
    void checkIfBlocked_whenAboveMaxAttempts_shouldThrowAccountLockedException() {
        when(loginAttemptRepository.countRecentAttempts(eq(TEST_USERNAME), any(Instant.class)))
                .thenReturn(5);
        when(loginAttemptRepository.findMostRecentAttempt(TEST_USERNAME))
                .thenReturn(Optional.of(new LoginAttempt(TEST_USERNAME, Instant.now())));

        assertThatThrownBy(() -> loginAttemptService.checkIfBlocked(TEST_USERNAME))
                .isInstanceOf(AccountLockedException.class);
    }

    @Test
    @Order(104)
    void checkIfBlocked_whenDisabled_shouldNotCheck() {
        when(properties.isEnabled()).thenReturn(false);

        // Should not throw even with max attempts
        loginAttemptService.checkIfBlocked(TEST_USERNAME);

        // Should not query repository
        verify(loginAttemptRepository, never()).countRecentAttempts(any(), any());
    }

    // =========================================================================
    // recordFailedAttempt TESTS (200s)
    // =========================================================================

    @Test
    @Order(201)
    void recordFailedAttempt_shouldSaveAttempt() {
        when(loginAttemptRepository.countRecentAttempts(eq(TEST_USERNAME), any(Instant.class)))
                .thenReturn(1);

        loginAttemptService.recordFailedAttempt(TEST_USERNAME, TEST_IP);

        ArgumentCaptor<LoginAttempt> captor = ArgumentCaptor.forClass(LoginAttempt.class);
        verify(loginAttemptRepository).save(captor.capture());

        LoginAttempt saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(saved.getIpAddress()).isEqualTo(TEST_IP);
        assertThat(saved.getAttemptTime()).isNotNull();
    }

    @Test
    @Order(202)
    void recordFailedAttempt_whenDisabled_shouldNotSave() {
        when(properties.isEnabled()).thenReturn(false);

        loginAttemptService.recordFailedAttempt(TEST_USERNAME, TEST_IP);

        verify(loginAttemptRepository, never()).save(any());
    }

    // =========================================================================
    // clearAttempts TESTS (300s)
    // =========================================================================

    @Test
    @Order(301)
    void clearAttempts_shouldDeleteByUsername() {
        loginAttemptService.clearAttempts(TEST_USERNAME);

        verify(loginAttemptRepository).deleteByUsername(TEST_USERNAME);
    }

    @Test
    @Order(302)
    void clearAttempts_whenDisabled_shouldNotDelete() {
        when(properties.isEnabled()).thenReturn(false);

        loginAttemptService.clearAttempts(TEST_USERNAME);

        verify(loginAttemptRepository, never()).deleteByUsername(any());
    }

    // =========================================================================
    // getRemainingAttempts TESTS (400s)
    // =========================================================================

    @Test
    @Order(401)
    void getRemainingAttempts_shouldReturnCorrectCount() {
        when(loginAttemptRepository.countRecentAttempts(eq(TEST_USERNAME), any(Instant.class)))
                .thenReturn(1);

        int remaining = loginAttemptService.getRemainingAttempts(TEST_USERNAME);

        assertThat(remaining).isEqualTo(2); // 3 max - 1 used = 2 remaining
    }

    @Test
    @Order(402)
    void getRemainingAttempts_whenMaxReached_shouldReturnZero() {
        when(loginAttemptRepository.countRecentAttempts(eq(TEST_USERNAME), any(Instant.class)))
                .thenReturn(5);

        int remaining = loginAttemptService.getRemainingAttempts(TEST_USERNAME);

        assertThat(remaining).isEqualTo(0);
    }

    @Test
    @Order(403)
    void getRemainingAttempts_whenDisabled_shouldReturnMaxValue() {
        when(properties.isEnabled()).thenReturn(false);

        int remaining = loginAttemptService.getRemainingAttempts(TEST_USERNAME);

        assertThat(remaining).isEqualTo(Integer.MAX_VALUE);
    }
}
