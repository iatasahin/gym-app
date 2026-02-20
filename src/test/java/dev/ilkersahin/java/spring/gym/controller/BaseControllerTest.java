package dev.ilkersahin.java.spring.gym.controller;

import dev.ilkersahin.java.spring.gym.exception.UnauthorizedAccessException;
import dev.ilkersahin.java.spring.gym.security.Role;
import dev.ilkersahin.java.spring.gym.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BaseControllerTest {

    private BaseController baseController;

    @BeforeEach
    void setUp() {
        baseController = new BaseController() {
            // Anonymous concrete implementation for testing
        };
    }

    // =========================================================================
    // GET AUTHENTICATED USERNAME TESTS (100s)
    // =========================================================================

    @ParameterizedTest
    @ValueSource(strings = {
        "John.Doe",
        "Jane.Smith",
        "User.Name123"
    })
    @Order(101)
    void getAuthenticatedUsername_withVariousUsernames_shouldReturnCorrectUsername(String mockedUsername) {
        try (var mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            when(SecurityUtils.getCurrentUsername()).thenReturn(Optional.of(mockedUsername));
            String username = baseController.getAuthenticatedUsername();
            assertThat(username).isEqualTo(mockedUsername);
        }
    }

    @Test
    @Order(102)
    void getAuthenticatedUsername_withNullAttribute_shouldThrowUnauthorizedAccessException() {
        try (var mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            when(SecurityUtils.getCurrentUsername()).thenReturn(Optional.empty());

            assertThatThrownBy(() -> baseController.getAuthenticatedUsername())
                    .isInstanceOf(UnauthorizedAccessException.class)
                    .hasMessage("No authenticated user");
        }
    }

    // =========================================================================
    // GET AUTHENTICATED ROLE TESTS (200s)
    // =========================================================================

    @Order(201)
    @ParameterizedTest
    @ValueSource(strings = {"TRAINEE", "TRAINER"})
    void getAuthenticatedRole_withTraineeRole_shouldReturnTrainee(String mockedRole) {
        try (var mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            when(SecurityUtils.getCurrentRole()).thenReturn(Optional.of(Role.valueOf(mockedRole)));

            Optional<Role> role = baseController.getAuthenticatedRole();

            assertThat(role).isPresent();
            assertThat(role).contains(Role.valueOf(mockedRole));
        }
    }

    @Test
    @Order(202)
    void getAuthenticatedRole_withNullAttribute_shouldReturnUnknown() {
        try (var mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            when(SecurityUtils.getCurrentRole()).thenReturn(Optional.empty());

            Optional<Role> role = baseController.getAuthenticatedRole();

            assertThat(role).isEmpty();
        }
    }

    // =========================================================================
    // VERIFY USER ACCESS TESTS (300s)
    // =========================================================================

    @ParameterizedTest
    @CsvSource({
        "John.Doe,John.Doe,false",
        "John.Doe,Jane.Smith,true",
        "john.doe,John.Doe,true",
        "Jane.Smith,Jane.Smith,false"
    })
    @Order(301)
    void verifyUserAccess_variousCases(String authenticatedUsername, String requestedUsername, boolean shouldThrow) {
        try (var mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            when(SecurityUtils.getCurrentUsername()).thenReturn(Optional.of(authenticatedUsername));
            if (shouldThrow) {
                assertThatThrownBy(() -> baseController.verifyUserAccess(requestedUsername))
                        .isInstanceOf(UnauthorizedAccessException.class);
            } else {
                baseController.verifyUserAccess(requestedUsername);
            }
        }
    }

    @Test
    @Order(302)
    void verifyUserAccess_withNoAuthenticatedUser_shouldThrowUnauthorizedAccessException() {
        try (var mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            when(SecurityUtils.getCurrentUsername()).thenReturn(Optional.empty());
            assertThatThrownBy(() -> baseController.verifyUserAccess("John.Doe"))
                    .isInstanceOf(UnauthorizedAccessException.class)
                    .hasMessage("No authenticated user");
        }
    }

    @Test
    @Order(303)
    void verifyUserAccess_withSameUsernameMultipleTimes_shouldNotThrowException() {
        try (var mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            when(SecurityUtils.getCurrentUsername()).thenReturn(Optional.of("John.Doe"));
            baseController.verifyUserAccess("John.Doe");
            baseController.verifyUserAccess("John.Doe");
            baseController.verifyUserAccess("John.Doe");
        }
    }

    @Test
    @Order(304)
    void verifyUserAccess_withWhitespaceInUsername_shouldHandleCorrectly() {
        try (var mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            when(SecurityUtils.getCurrentUsername()).thenReturn(Optional.of("John.Doe"));
            assertThatThrownBy(() -> baseController.verifyUserAccess(" John.Doe"))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }
    }


    @Test
    @Order(305)
    void verifyUserAccess_withEmptyRequestedUsername_shouldThrowException() {
        try (var mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            when(SecurityUtils.getCurrentUsername()).thenReturn(Optional.of("John.Doe"));
            assertThatThrownBy(() -> baseController.verifyUserAccess(""))
                    .isInstanceOf(UnauthorizedAccessException.class)
                    .hasMessage("User 'John.Doe' cannot access resources of ''");
        }
    }

    // =========================================================================
    // INTEGRATION TESTS - COMBINING METHODS (400s)
    // =========================================================================

    @Test
    @Order(401)
    void getAuthenticatedUsernameAndRole_bothSet_shouldReturnBothCorrectly() {
        try (var mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            when(SecurityUtils.getCurrentUsername()).thenReturn(Optional.of("John.Doe"));
            when(SecurityUtils.getCurrentRole()).thenReturn(Optional.of(Role.TRAINEE));

            String username = baseController.getAuthenticatedUsername();
            Optional<Role> role = baseController.getAuthenticatedRole();

            assertThat(username).isEqualTo("John.Doe");
            assertThat(role).isPresent();
            assertThat(role).contains(Role.TRAINEE);
        }
    }

    @Test
    @Order(402)
    void getAuthenticatedUsernameAndRole_onlyUsernameSet_shouldReturnUsernameAndEmptyRole() {
        try (var mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            when(SecurityUtils.getCurrentUsername()).thenReturn(Optional.of("John.Doe"));
            when(SecurityUtils.getCurrentRole()).thenReturn(Optional.empty());
            String username = baseController.getAuthenticatedUsername();
            Optional<Role> role = baseController.getAuthenticatedRole();

            assertThat(username).isEqualTo("John.Doe");
            assertThat(role).isEmpty();
        }
    }

    @Test
    @Order(403)
    void verifyAccessThenGetRole_validUser_shouldWorkCorrectly() {
        try (var mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            when(SecurityUtils.getCurrentUsername()).thenReturn(Optional.of("Jane.Smith"));
            when(SecurityUtils.getCurrentRole()).thenReturn(Optional.of(Role.TRAINER));

            // Verify access first
            baseController.verifyUserAccess("Jane.Smith");

            // Then get role
            Optional<Role> role = baseController.getAuthenticatedRole();
            assertThat(role).isPresent();
            assertThat(role).contains(Role.TRAINER);
        }
    }
}
