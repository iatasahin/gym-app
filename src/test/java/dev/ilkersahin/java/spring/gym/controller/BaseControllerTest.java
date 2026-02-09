package dev.ilkersahin.java.spring.gym.controller;

import dev.ilkersahin.java.spring.gym.exception.UnauthorizedAccessException;
import dev.ilkersahin.java.spring.gym.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BaseControllerTest {

    @Mock private HttpServletRequest httpServletRequest;

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

    @Test
    @Order(101)
    void getAuthenticatedUsername_withValidUsername_shouldReturnUsername() {
        when(httpServletRequest.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_USERNAME))
                .thenReturn("John.Doe");

        String username = baseController.getAuthenticatedUsername(httpServletRequest);

        assertThat(username).isEqualTo("John.Doe");
    }

    @Test
    @Order(102)
    void getAuthenticatedUsername_withDifferentUsername_shouldReturnCorrectUsername() {
        when(httpServletRequest.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_USERNAME))
                .thenReturn("Jane.Smith");

        String username = baseController.getAuthenticatedUsername(httpServletRequest);

        assertThat(username).isEqualTo("Jane.Smith");
    }

    @Test
    @Order(103)
    void getAuthenticatedUsername_withNullAttribute_shouldThrowUnauthorizedAccessException() {
        when(httpServletRequest.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_USERNAME))
                .thenReturn(null);

        assertThatThrownBy(() -> baseController.getAuthenticatedUsername(httpServletRequest))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage("No authenticated user");
    }

    @Test
    @Order(104)
    void getAuthenticatedUsername_withUsernameContainingSpecialCharacters_shouldReturnUsername() {
        when(httpServletRequest.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_USERNAME))
                .thenReturn("User.Name123");

        String username = baseController.getAuthenticatedUsername(httpServletRequest);

        assertThat(username).isEqualTo("User.Name123");
    }

    // =========================================================================
    // GET AUTHENTICATED ROLE TESTS (200s)
    // =========================================================================

    @Test
    @Order(201)
    void getAuthenticatedRole_withTraineeRole_shouldReturnTrainee() {
        when(httpServletRequest.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_ROLE))
                .thenReturn("TRAINEE");

        String role = baseController.getAuthenticatedRole(httpServletRequest);

        assertThat(role).isEqualTo("TRAINEE");
    }

    @Test
    @Order(202)
    void getAuthenticatedRole_withTrainerRole_shouldReturnTrainer() {
        when(httpServletRequest.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_ROLE))
                .thenReturn("TRAINER");

        String role = baseController.getAuthenticatedRole(httpServletRequest);

        assertThat(role).isEqualTo("TRAINER");
    }

    @Test
    @Order(203)
    void getAuthenticatedRole_withNullAttribute_shouldReturnUnknown() {
        when(httpServletRequest.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_ROLE))
                .thenReturn(null);

        String role = baseController.getAuthenticatedRole(httpServletRequest);

        assertThat(role).isEqualTo("UNKNOWN");
    }

    @Test
    @Order(204)
    void getAuthenticatedRole_withAdminRole_shouldReturnAdmin() {
        when(httpServletRequest.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_ROLE))
                .thenReturn("ADMIN");

        String role = baseController.getAuthenticatedRole(httpServletRequest);

        assertThat(role).isEqualTo("ADMIN");
    }

    @Test
    @Order(205)
    void getAuthenticatedRole_withCustomRole_shouldReturnCustomRole() {
        when(httpServletRequest.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_ROLE))
                .thenReturn("CUSTOM_ROLE");

        String role = baseController.getAuthenticatedRole(httpServletRequest);

        assertThat(role).isEqualTo("CUSTOM_ROLE");
    }

    // =========================================================================
    // VERIFY USER ACCESS TESTS (300s)
    // =========================================================================

    @Test
    @Order(301)
    void verifyUserAccess_withMatchingUsername_shouldNotThrowException() {
        when(httpServletRequest.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_USERNAME))
                .thenReturn("John.Doe");

        // Should not throw any exception
        baseController.verifyUserAccess(httpServletRequest, "John.Doe");
    }

    @Test
    @Order(302)
    void verifyUserAccess_withNonMatchingUsername_shouldThrowUnauthorizedAccessException() {
        when(httpServletRequest.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_USERNAME))
                .thenReturn("John.Doe");

        assertThatThrownBy(() -> baseController.verifyUserAccess(httpServletRequest, "Jane.Smith"))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage("User 'John.Doe' cannot access resources of 'Jane.Smith'");
    }

    @Test
    @Order(303)
    void verifyUserAccess_withNoAuthenticatedUser_shouldThrowUnauthorizedAccessException() {
        when(httpServletRequest.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_USERNAME))
                .thenReturn(null);

        assertThatThrownBy(() -> baseController.verifyUserAccess(httpServletRequest, "John.Doe"))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage("No authenticated user");
    }

    @Test
    @Order(304)
    void verifyUserAccess_withCaseSensitiveUsernames_shouldThrowException() {
        when(httpServletRequest.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_USERNAME))
                .thenReturn("john.doe");

        assertThatThrownBy(() -> baseController.verifyUserAccess(httpServletRequest, "John.Doe"))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage("User 'john.doe' cannot access resources of 'John.Doe'");
    }

    @Test
    @Order(305)
    void verifyUserAccess_withSameUsernameMultipleTimes_shouldNotThrowException() {
        when(httpServletRequest.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_USERNAME))
                .thenReturn("John.Doe");

        // Multiple calls should all succeed
        baseController.verifyUserAccess(httpServletRequest, "John.Doe");
        baseController.verifyUserAccess(httpServletRequest, "John.Doe");
        baseController.verifyUserAccess(httpServletRequest, "John.Doe");
    }

    @Test
    @Order(306)
    void verifyUserAccess_withWhitespaceInUsername_shouldHandleCorrectly() {
        when(httpServletRequest.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_USERNAME))
                .thenReturn("John.Doe");

        assertThatThrownBy(() -> baseController.verifyUserAccess(httpServletRequest, " John.Doe"))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    @Order(307)
    void verifyUserAccess_authenticatedUserAccessingOwnResource_shouldSucceed() {
        when(httpServletRequest.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_USERNAME))
                .thenReturn("Jane.Smith");

        // This should complete without throwing
        baseController.verifyUserAccess(httpServletRequest, "Jane.Smith");
    }

    @Test
    @Order(308)
    void verifyUserAccess_withEmptyRequestedUsername_shouldThrowException() {
        when(httpServletRequest.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_USERNAME))
                .thenReturn("John.Doe");

        assertThatThrownBy(() -> baseController.verifyUserAccess(httpServletRequest, ""))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage("User 'John.Doe' cannot access resources of ''");
    }

    // =========================================================================
    // INTEGRATION TESTS - COMBINING METHODS (400s)
    // =========================================================================

    @Test
    @Order(401)
    void getAuthenticatedUsernameAndRole_bothSet_shouldReturnBothCorrectly() {
        when(httpServletRequest.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_USERNAME))
                .thenReturn("John.Doe");
        when(httpServletRequest.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_ROLE))
                .thenReturn("TRAINEE");

        String username = baseController.getAuthenticatedUsername(httpServletRequest);
        String role = baseController.getAuthenticatedRole(httpServletRequest);

        assertThat(username).isEqualTo("John.Doe");
        assertThat(role).isEqualTo("TRAINEE");
    }

    @Test
    @Order(402)
    void getAuthenticatedUsernameAndRole_onlyUsernameSet_shouldReturnUsernameAndUnknownRole() {
        when(httpServletRequest.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_USERNAME))
                .thenReturn("John.Doe");
        when(httpServletRequest.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_ROLE))
                .thenReturn(null);

        String username = baseController.getAuthenticatedUsername(httpServletRequest);
        String role = baseController.getAuthenticatedRole(httpServletRequest);

        assertThat(username).isEqualTo("John.Doe");
        assertThat(role).isEqualTo("UNKNOWN");
    }

    @Test
    @Order(403)
    void verifyAccessThenGetRole_validUser_shouldWorkCorrectly() {
        when(httpServletRequest.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_USERNAME))
                .thenReturn("Jane.Smith");
        when(httpServletRequest.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_ROLE))
                .thenReturn("TRAINER");

        // Verify access first
        baseController.verifyUserAccess(httpServletRequest, "Jane.Smith");

        // Then get role
        String role = baseController.getAuthenticatedRole(httpServletRequest);
        assertThat(role).isEqualTo("TRAINER");
    }
}
