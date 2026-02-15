package dev.ilkersahin.java.spring.gym.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;
    @Mock private AuthContextImpl authContextImpl;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        responseWriter = new StringWriter();
        lenient().when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    // =========================================================================
    // PUBLIC ENDPOINT TESTS (100s)
    // =========================================================================

    @ParameterizedTest(name = "[{index}] {0} {1} should pass through without authentication")
    @CsvSource({
            "POST, /api/v1/auth/login",
            "GET,  /health",
            "GET,  /api/v1/training-types",
            "GET,  /swagger-ui/index.html",
            "GET,  /webjars/swagger-ui/swagger-ui.css",
            "GET,  /api-docs",
            "POST, /api/v1/trainees",
            "POST, /api/v1/trainers"
    })
    @Order(101)
    void doFilter_publicEndpoints_shouldPassThrough(String method, String uri) throws Exception {
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getMethod()).thenReturn(method);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    // =========================================================================
    // MISSING AUTH HEADER TESTS (200s)
    // =========================================================================

    @Test
    @Order(201)
    void doFilter_protectedEndpointNoAuthHeader_shouldReturn401() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/trainees/John.Doe");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(401);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @Order(202)
    void doFilter_invalidAuthHeaderFormat_shouldReturn401() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/trainees/John.Doe");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Basic credentials");

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(401);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @Order(203)
    void doFilter_emptyBearerToken_shouldValidateEmptyString() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/trainees/John.Doe");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        when(jwtService.validateAndGetUsername("")).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtService).validateAndGetUsername("");
        verify(response).setStatus(401);
    }

    // =========================================================================
    // VALID TOKEN TESTS (300s)
    // =========================================================================

    @ParameterizedTest(name = "[{index}] {0} should authenticate as {1} with role {2}")
    @CsvSource({
            "/api/v1/trainees/John.Doe, John.Doe, TRAINEE",
            "/api/v1/trainers/Jane.Smith, Jane.Smith, TRAINER"
    })
    @Order(301)
    void doFilter_validToken_shouldSetUsernameAndRole(String uri, String username, String roleStr) throws Exception {
        String token = "validToken";
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateAndGetUsername(token)).thenReturn(Optional.of(username));
        when(jwtService.getRole(token)).thenReturn(Optional.of(Role.valueOf(roleStr)));

        filter.doFilterInternal(request, response, filterChain);

        verify(request).setAttribute(JwtAuthenticationFilter.AUTHENTICATED_USERNAME, username);
        verify(request).setAttribute(JwtAuthenticationFilter.AUTHENTICATED_ROLE, Role.valueOf(roleStr));
        verify(filterChain).doFilter(request, response);
        verify(authContextImpl).setUsername(username);
        verify(authContextImpl).setAuthenticated(true);
        verify(authContextImpl).setRole(Role.valueOf(roleStr));
    }

    @Test
    @Order(302)
    void doFilter_validTokenNoRole_shouldStillPassThrough() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/trainees/John.Doe");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");
        when(jwtService.validateAndGetUsername("validToken")).thenReturn(Optional.of("John.Doe"));
        when(jwtService.getRole("validToken")).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        verify(request).setAttribute(JwtAuthenticationFilter.AUTHENTICATED_USERNAME, "John.Doe");
        verify(request, never()).setAttribute(eq(JwtAuthenticationFilter.AUTHENTICATED_ROLE), any());
        verify(filterChain).doFilter(request, response);
        verify(authContextImpl).setUsername("John.Doe");
        verify(authContextImpl).setAuthenticated(true);
        verify(authContextImpl, never()).setRole(any());
    }

    // =========================================================================
    // INVALID TOKEN TESTS (400s)
    // =========================================================================

    @Test
    @Order(401)
    void doFilter_invalidToken_shouldReturn401() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/trainees/John.Doe");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidToken");
        when(jwtService.validateAndGetUsername("invalidToken")).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(401);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @Order(402)
    void doFilter_expiredToken_shouldReturn401() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/trainers/Jane.Smith");
        when(request.getMethod()).thenReturn("PUT");
        when(request.getHeader("Authorization")).thenReturn("Bearer expiredToken");
        when(jwtService.validateAndGetUsername("expiredToken")).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(401);
        verify(filterChain, never()).doFilter(request, response);
    }

    // =========================================================================
    // NON-PUBLIC POST/GET ENDPOINT TESTS (500s)
    // =========================================================================

    @Test
    @Order(501)
    void doFilter_traineeGetNotPost_shouldRequireAuth() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/trainees");
        when(request.getMethod()).thenReturn("GET"); // GET on /trainees is NOT public
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(401);
    }

    @Test
    @Order(502)
    void doFilter_trainingsPostEndpoint_shouldRequireAuth() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/trainings");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(401);
    }

    @Test
    @Order(503)
    void doFilter_traineeSubresource_shouldRequireAuth() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/trainees/John.Doe/trainings");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(401);
    }
}
