package dev.ilkersahin.java.spring.gym.security;

import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // =========================================================================
    // getCurrentUsername TESTS (100s)
    // =========================================================================

    @Test
    @Order(101)
    void getCurrentUsername_whenAuthenticated_shouldReturnUsername() {
        setAuthentication("test.user", "ROLE_TRAINEE");

        Optional<String> username = SecurityUtils.getCurrentUsername();

        assertThat(username).isPresent().contains("test.user");
    }

    @Test
    @Order(102)
    void getCurrentUsername_whenNotAuthenticated_shouldReturnEmpty() {
        // No authentication set

        Optional<String> username = SecurityUtils.getCurrentUsername();

        assertThat(username).isEmpty();
    }

    @Test
    @Order(103)
    void getCurrentUsername_whenAnonymous_shouldReturnEmpty() {
        setAnonymousAuthentication();

        Optional<String> username = SecurityUtils.getCurrentUsername();

        assertThat(username).isEmpty();
    }

    // =========================================================================
    // getCurrentRole TESTS (200s)
    // =========================================================================

    @Test
    @Order(201)
    void getCurrentRole_whenTrainee_shouldReturnTrainee() {
        setAuthentication("test.user", "ROLE_TRAINEE");

        Optional<Role> role = SecurityUtils.getCurrentRole();

        assertThat(role).isPresent().contains(Role.TRAINEE);
    }

    @Test
    @Order(202)
    void getCurrentRole_whenTrainer_shouldReturnTrainer() {
        setAuthentication("test.user", "ROLE_TRAINER");

        Optional<Role> role = SecurityUtils.getCurrentRole();

        assertThat(role).isPresent().contains(Role.TRAINER);
    }

    @Test
    @Order(203)
    void getCurrentRole_whenNotAuthenticated_shouldReturnEmpty() {
        Optional<Role> role = SecurityUtils.getCurrentRole();

        assertThat(role).isEmpty();
    }

    // =========================================================================
    // hasRole TESTS (300s)
    // =========================================================================

    @Test
    @Order(301)
    void hasRole_whenHasRole_shouldReturnTrue() {
        setAuthentication("test.user", "ROLE_TRAINEE");

        assertThat(SecurityUtils.hasRole(Role.TRAINEE)).isTrue();
    }

    @Test
    @Order(302)
    void hasRole_whenDoesNotHaveRole_shouldReturnFalse() {
        setAuthentication("test.user", "ROLE_TRAINEE");

        assertThat(SecurityUtils.hasRole(Role.TRAINER)).isFalse();
    }

    @Test
    @Order(303)
    void hasRole_whenNotAuthenticated_shouldReturnFalse() {
        assertThat(SecurityUtils.hasRole(Role.TRAINEE)).isFalse();
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private void setAuthentication(String username, String... roles) {
        List<SimpleGrantedAuthority> authorities = Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .toList();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void setAnonymousAuthentication() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("anonymousUser", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
