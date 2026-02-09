package dev.ilkersahin.java.spring.gym.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Test
    @Order(101)
    void doFilter_loginEndpoint_shouldPassThrough() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
        when(request.getMethod()).thenReturn("POST");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    @Order(102)
    void doFilter_healthEndpoint_shouldPassThrough() throws Exception {
        when(request.getRequestURI()).thenReturn("/health");
        when(request.getMethod()).thenReturn("GET");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @Order(103)
    void doFilter_trainingTypesEndpoint_shouldPassThrough() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/training-types");
        when(request.getMethod()).thenReturn("GET");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @Order(104)
    void doFilter_swaggerEndpoint_shouldPassThrough() throws Exception {
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");
        when(request.getMethod()).thenReturn("GET");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @Order(105)
    void doFilter_webjarsEndpoint_shouldPassThrough() throws Exception {
        when(request.getRequestURI()).thenReturn("/webjars/swagger-ui/swagger-ui.css");
        when(request.getMethod()).thenReturn("GET");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @Order(106)
    void doFilter_apiDocsEndpoint_shouldPassThrough() throws Exception {
        when(request.getRequestURI()).thenReturn("/api-docs");
        when(request.getMethod()).thenReturn("GET");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @Order(107)
    void doFilter_traineeRegistrationPost_shouldPassThrough() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/trainees");
        when(request.getMethod()).thenReturn("POST");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @Order(108)
    void doFilter_trainerRegistrationPost_shouldPassThrough() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/trainers");
        when(request.getMethod()).thenReturn("POST");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
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

    @Test
    @Order(301)
    void doFilter_validToken_shouldSetUsernameAndRole() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/trainees/John.Doe");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken123");
        when(jwtService.validateAndGetUsername("validToken123")).thenReturn(Optional.of("John.Doe"));
        when(jwtService.getRole("validToken123")).thenReturn(Optional.of("TRAINEE"));

        filter.doFilterInternal(request, response, filterChain);

        verify(request).setAttribute(JwtAuthenticationFilter.AUTHENTICATED_USERNAME, "John.Doe");
        verify(request).setAttribute(JwtAuthenticationFilter.AUTHENTICATED_ROLE, "TRAINEE");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @Order(302)
    void doFilter_validTokenTrainer_shouldSetTrainerRole() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/trainers/Jane.Smith");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");
        when(jwtService.validateAndGetUsername("validToken")).thenReturn(Optional.of("Jane.Smith"));
        when(jwtService.getRole("validToken")).thenReturn(Optional.of("TRAINER"));

        filter.doFilterInternal(request, response, filterChain);

        verify(request).setAttribute(JwtAuthenticationFilter.AUTHENTICATED_USERNAME, "Jane.Smith");
        verify(request).setAttribute(JwtAuthenticationFilter.AUTHENTICATED_ROLE, "TRAINER");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @Order(303)
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
