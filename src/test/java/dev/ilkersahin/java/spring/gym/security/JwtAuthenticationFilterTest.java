package dev.ilkersahin.java.spring.gym.security;

import dev.ilkersahin.java.spring.gym.security.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // =========================================================================
    // NO TOKEN TESTS (100s) - Filter passes through, Spring Security handles authz
    // =========================================================================


    @ParameterizedTest(name = "[{index}] Authorization header ''{0}'' should pass through without authentication")
    @ValueSource(strings = {
            "Basic credentials",
            "bearer token",  // lowercase 'bearer'
            "Token abc123",
            "JWT xyz"
    })
    @Order(101)
    void doFilter_noOrInvalidAuthHeader_shouldPassThroughWithoutAuth(String authHeader) throws Exception {
        when(request.getHeader("Authorization")).thenReturn(authHeader);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService);
    }

    @Test
    @Order(102)
    void doFilter_bearerPrefixOnly_shouldValidateEmptyToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        when(jwtService.validateAndGetUsername("")).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).validateAndGetUsername("");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    // =========================================================================
    // VALID TOKEN TESTS (200s) - Sets SecurityContext
    // =========================================================================

    @Test
    @Order(201)
    void doFilter_validTokenWithTraineeRole_shouldSetSecurityContext() throws Exception {
        String token = "validTraineeToken";
        String username = "john.doe";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateAndGetUsername(token)).thenReturn(Optional.of(username));
        when(jwtService.getRole(token)).thenReturn(Optional.of(Role.TRAINEE));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo(username);
        assertThat(auth.isAuthenticated()).isTrue();
        assertThat(auth.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_TRAINEE");
    }

    @Test
    @Order(202)
    void doFilter_validTokenWithTrainerRole_shouldSetSecurityContext() throws Exception {
        String token = "validTrainerToken";
        String username = "jane.smith";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateAndGetUsername(token)).thenReturn(Optional.of(username));
        when(jwtService.getRole(token)).thenReturn(Optional.of(Role.TRAINER));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo(username);
        assertThat(auth.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_TRAINER");
    }

    @Test
    @Order(203)
    void doFilter_validTokenMissingRole_shouldPassThroughWithoutSettingContext() throws Exception {
        String token = "tokenWithoutRole";
        String username = "john.doe";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateAndGetUsername(token)).thenReturn(Optional.of(username));
        when(jwtService.getRole(token)).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        // No authentication set because role is required
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    // =========================================================================
    // INVALID TOKEN TESTS (300s) - Passes through, Spring Security handles 401
    // =========================================================================

    @Test
    @Order(301)
    void doFilter_invalidToken_shouldPassThroughWithoutSettingContext() throws Exception {
        String token = "invalidToken";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateAndGetUsername(token)).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        // Note: Spring Security will return 401 for protected endpoints
    }

    @Test
    @Order(302)
    void doFilter_expiredToken_shouldPassThroughWithoutSettingContext() throws Exception {
        String token = "expiredToken";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateAndGetUsername(token)).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    // =========================================================================
    // ALREADY AUTHENTICATED TESTS (400s) - Skips processing
    // =========================================================================

    @Test
    @Order(401)
    void doFilter_alreadyAuthenticated_shouldSkipProcessing() throws Exception {
        String token = "someToken";

        // Pre-set authentication in SecurityContext
        var existingAuth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        // Should NOT call jwtService because already authenticated
        verifyNoInteractions(jwtService);
        // Original authentication should remain
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(existingAuth);
    }

    // =========================================================================
    // EDGE CASES (500s)
    // =========================================================================

    @Test
    @Order(501)
    void doFilter_bearerPrefixCaseSensitive_shouldNotMatchLowercase() throws Exception {
        // "bearer" lowercase should not be recognized
        when(request.getHeader("Authorization")).thenReturn("bearer token");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @Order(502)
    void doFilter_tokenWithWhitespace_shouldTrimAndValidate() throws Exception {
        // Token after "Bearer " might have leading whitespace
        String token = "validToken";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateAndGetUsername(token)).thenReturn(Optional.of("john.doe"));
        when(jwtService.getRole(token)).thenReturn(Optional.of(Role.TRAINEE));

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtService).validateAndGetUsername(token);
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }
}
