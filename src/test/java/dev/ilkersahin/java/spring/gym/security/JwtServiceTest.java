package dev.ilkersahin.java.spring.gym.security;

import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JwtServiceTest {

    private JwtService jwtService;

    private static final String TEST_SECRET = "myTestSecretKeyThatIsAtLeast32BytesLongForHS256";
    private static final long TEST_EXPIRATION_MS = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", TEST_EXPIRATION_MS);
        jwtService.init();
    }


    // =========================================================================
    // GENERATE TOKEN TESTS (100s)
    // =========================================================================

    @Test
    @Order(101)
    void generateToken_withValidInput_shouldReturnNonNullToken() {
        String token = jwtService.generateToken("testUser", Role.TRAINEE);

        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @Order(102)
    void generateToken_shouldReturnValidJwtFormat() {
        String token = jwtService.generateToken("testUser", Role.TRAINEE);

        // JWT format: header.payload.signature
        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);
    }

    @Test
    @Order(103)
    void generateToken_withDifferentUsers_shouldReturnDifferentTokens() {
        String token1 = jwtService.generateToken("user1", Role.TRAINEE);
        String token2 = jwtService.generateToken("user2", Role.TRAINER);

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @Order(104)
    void generateToken_withSameUserDifferentRoles_shouldReturnDifferentTokens() {
        String token1 = jwtService.generateToken("user1", Role.TRAINEE);
        String token2 = jwtService.generateToken("user1", Role.TRAINER);

        assertThat(token1).isNotEqualTo(token2);
    }

    // =========================================================================
    // VALIDATE AND GET USERNAME TESTS (200s)
    // =========================================================================

    @Test
    @Order(201)
    void validateAndGetUsername_withValidToken_shouldReturnUsername() {
        String token = jwtService.generateToken("testUser", Role.TRAINEE);

        Optional<String> result = jwtService.validateAndGetUsername(token);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("testUser");
    }

    @Test
    @Order(202)
    void validateAndGetUsername_withInvalidToken_shouldReturnEmpty() {
        Optional<String> result = jwtService.validateAndGetUsername("invalid.token.here");

        assertThat(result).isEmpty();
    }

    @Test
    @Order(203)
    void validateAndGetUsername_withTamperedToken_shouldReturnEmpty() {
        String token = jwtService.generateToken("testUser", Role.TRAINEE);
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        Optional<String> result = jwtService.validateAndGetUsername(tamperedToken);

        assertThat(result).isEmpty();
    }

    @Test
    @Order(204)
    void validateAndGetUsername_withExpiredToken_shouldReturnEmpty() throws InterruptedException {
        // Create service with very short expiration
        JwtService shortLivedService = new JwtService();
        ReflectionTestUtils.setField(shortLivedService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(shortLivedService, "expirationMs", 1L);
        shortLivedService.init();

        String token = shortLivedService.generateToken("testUser", Role.TRAINEE);
        Thread.sleep(50); // Wait for expiration

        Optional<String> result = shortLivedService.validateAndGetUsername(token);

        assertThat(result).isEmpty();
    }

    // =========================================================================
    // GET ROLE TESTS (300s)
    // =========================================================================

    @Test
    @Order(301)
    void getRole_withTraineeToken_shouldReturnTrainee() {
        String token = jwtService.generateToken("testUser", Role.TRAINEE);

        Optional<Role> result = jwtService.getRole(token);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(Role.TRAINEE);
    }

    @Test
    @Order(302)
    void getRole_withTrainerToken_shouldReturnTrainer() {
        String token = jwtService.generateToken("testUser", Role.TRAINER);

        Optional<Role> result = jwtService.getRole(token);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(Role.TRAINER);
    }

    @Test
    @Order(303)
    void getRole_withInvalidToken_shouldReturnEmpty() {
        Optional<Role> result = jwtService.getRole("invalid.token.here");

        assertThat(result).isEmpty();
    }

    @Test
    @Order(304)
    void getRole_withTamperedToken_shouldReturnEmpty() {
        String token = jwtService.generateToken("testUser", Role.TRAINEE);
        String tamperedToken = token + "tampered";

        Optional<Role> result = jwtService.getRole(tamperedToken);

        assertThat(result).isEmpty();
    }

    // =========================================================================
    // EXTRACT USERNAME UNSAFE TESTS (400s)
    // =========================================================================

    @Test
    @Order(401)
    void extractUsernameUnsafe_withValidToken_shouldReturnUsername() {
        String token = jwtService.generateToken("testUser", Role.TRAINEE);

        String result = jwtService.extractUsernameUnsafe(token);

        assertThat(result).isEqualTo("testUser");
    }

    @Test
    @Order(402)
    void extractUsernameUnsafe_withInvalidToken_shouldReturnUnknown() {
        String result = jwtService.extractUsernameUnsafe("not-a-valid-token");

        assertThat(result).isEqualTo("unknown");
    }

    @Test
    @Order(403)
    void extractUsernameUnsafe_withEmptyToken_shouldReturnUnknown() {
        String result = jwtService.extractUsernameUnsafe("");

        assertThat(result).isEqualTo("unknown");
    }

    @Test
    @Order(404)
    void extractUsernameUnsafe_withMalformedBase64_shouldReturnUnknown() {
        String result = jwtService.extractUsernameUnsafe("header.!!!invalid-base64!!!.signature");

        assertThat(result).isEqualTo("unknown");
    }

    @Test
    @Order(405)
    void extractUsernameUnsafe_withValidFormatButNoSubject_shouldReturnUnknown() {
        // Valid base64 but no "sub" field
        String payload = java.util.Base64.getUrlEncoder().encodeToString("{\"role\":\"TRAINEE\"}".getBytes());
        String fakeToken = "header." + payload + ".signature";

        String result = jwtService.extractUsernameUnsafe(fakeToken);

        assertThat(result).isEqualTo("unknown");
    }
}
