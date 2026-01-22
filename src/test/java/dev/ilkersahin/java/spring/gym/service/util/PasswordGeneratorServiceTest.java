package dev.ilkersahin.java.spring.gym.service.util;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PasswordGeneratorServiceTest {

    private final PasswordGeneratorService service = new PasswordGeneratorService();

    // === GENERATE PASSWORD TESTS ===

    @Test
    @Order(101)
    void generate_withValidLength_shouldReturnPasswordOfCorrectLength() {
        String pw = service.generate(10);

        assertThat(pw).hasSize(10);
    }

    @Test
    @Order(102)
    void generate_withDifferentLengths_shouldReturnPasswordsOfCorrectLengths() {
        assertThat(service.generate(1)).hasSize(1);
        assertThat(service.generate(5)).hasSize(5);
        assertThat(service.generate(20)).hasSize(20);
        assertThat(service.generate(50)).hasSize(50);
        assertThat(service.generate(100)).hasSize(100);
    }

    @Test
    @Order(103)
    void generate_withZeroLength_shouldReturnEmptyString() {
        String password = service.generate(0);

        assertThat(password).isEmpty();
    }

    @Test
    @Order(104)
    void generate_withNegativeLength_shouldThrowException() {
        assertThatThrownBy(() -> service.generate(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password length cannot be negative");

        assertThatThrownBy(() -> service.generate(-10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Order(105)
    void generate_withLargeLength_shouldReturnPasswordOfCorrectLength() {
        String password = service.generate(1000);

        assertThat(password).hasSize(1000);
    }

    // === CHARACTER VALIDATION TESTS ===

    @Test
    @Order(201)
    void generate_withAnyValidLength_shouldUseOnlyAllowedCharacters() {
        String pw = service.generate(100);

        assertThat(pw).matches("[A-Za-z0-9]+");
    }

    @Test
    @Order(202)
    void generate_withLargeLength_shouldContainUppercaseLetters() {
        String password = service.generate(100); // Large sample for statistical confidence

        assertThat(password).matches(".*[A-Z].*");
    }

    @Test
    @Order(203)
    void generate_withLargeLength_shouldContainLowercaseLetters() {
        String password = service.generate(100);

        assertThat(password).matches(".*[a-z].*");
    }

    @Test
    @Order(204)
    void generate_withLargeLength_shouldContainDigits() {
        String password = service.generate(100);

        assertThat(password).matches(".*[0-9].*");
    }

    @Test
    @Order(205)
    void generate_shouldNotContainSpecialCharacters() {
        String password = service.generate(100);

        assertThat(password).doesNotMatch(".*[^A-Za-z0-9].*");
    }

    @Test
    @Order(206)
    void generate_shouldNotContainSpaces() {
        String password = service.generate(100);

        assertThat(password).doesNotContain(" ");
    }

    // === RANDOMNESS AND UNIQUENESS TESTS ===

    @Test
    @Order(301)
    void generate_calledMultipleTimes_shouldGenerateDifferentPasswords() {
        Set<String> passwords = new HashSet<>();
        int iterations = 1000;

        for (int i = 0; i < iterations; i++) {
            passwords.add(service.generate(16));
        }

        assertThat(passwords).hasSizeGreaterThan((int) (iterations * 0.99));
    }


    @Test
    @Order(302)
    void generate_withShortLength_shouldStillGenerateUniquePasswords() {
        Set<String> passwords = new HashSet<>();
        int iterations = 100;

        for (int i = 0; i < iterations; i++) {
            passwords.add(service.generate(8));
        }

        assertThat(passwords).hasSizeGreaterThan((int) (iterations * 0.95));
    }

    @Test
    @Order(303)
    void generate_consecutiveCalls_shouldNotGenerateIdenticalPasswords() {
        String password1 = service.generate(20);
        String password2 = service.generate(20);
        String password3 = service.generate(20);

        assertThat(password1).isNotEqualTo(password2);
        assertThat(password2).isNotEqualTo(password3);
        assertThat(password1).isNotEqualTo(password3);
    }

    @Test
    @Order(304)
    void generate_withSameLength_shouldHaveGoodCharacterDistribution() {
        String password = service.generate(1000);

        // Count character types
        long uppercaseCount = password.chars().filter(Character::isUpperCase).count();
        long lowercaseCount = password.chars().filter(Character::isLowerCase).count();
        long digitCount = password.chars().filter(Character::isDigit).count();

        // With 26 uppercase, 26 lowercase, 10 digits (62 total)
        // Expected distribution: ~42% uppercase letters, ~42% lowercase letters , ~16% digits
        // Allow for reasonable variance (±10%)
        assertThat(uppercaseCount).isBetween(320L, 520L); // ~26/62 * 1000 ± variance   // 420 ± 100
        assertThat(lowercaseCount).isBetween(320L, 520L); // ~26/62 * 1000 ± variance   // 420 ± 100
        assertThat(digitCount).isBetween(60L, 260L);      // ~10/62 * 1000 ± variance   // 160 ± 100

        // Verify total
        assertThat(uppercaseCount + lowercaseCount + digitCount).isEqualTo(1000);
    }

    // === EDGE CASES AND BOUNDARY TESTS ===

    @Test
    @Order(401)
    void generate_withLength1_shouldReturnSingleCharacter() {
        String password = service.generate(1);

        assertThat(password)
                .hasSize(1)
                .matches("[A-Za-z0-9]");
    }

    @Test
    @Order(402)
    void generate_withLength2_shouldReturnTwoCharacters() {
        String password = service.generate(2);

        assertThat(password)
                .hasSize(2)
                .matches("[A-Za-z0-9]{2}");
    }

    @Test
    @Order(403)
    void generate_withLargeIntegerLength_shouldHandleGracefully() {
        // Test with a very large but reasonable length
        int largeLength = 10000;

        String password = service.generate(largeLength);

        assertThat(password)
                .hasSize(largeLength)
                .matches("[A-Za-z0-9]+");
    }

    @Test
    @Order(404)
    void generate_multipleCallsWithZeroLength_shouldAlwaysReturnEmpty() {
        for (int i = 0; i < 10; i++) {
            assertThat(service.generate(0)).isEmpty();
        }
    }

    // === PERFORMANCE AND STRESS TESTS (501+) ===

    @Test
    @Order(501)
    void generate_manyShortPasswords_shouldPerformWell() {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            String password = service.generate(10);
            assertThat(password).hasSize(10);
        }

        long duration = System.currentTimeMillis() - startTime;

        // Should complete within reasonable time (adjust threshold as needed)
        assertThat(duration).isLessThan(5000); // 5 seconds
    }

    @Test
    @Order(502)
    void generate_fewLongPasswords_shouldPerformWell() {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            String password = service.generate(1000);
            assertThat(password).hasSize(1000);
        }

        long duration = System.currentTimeMillis() - startTime;

        // Should complete within reasonable time
        assertThat(duration).isLessThan(3000); // 3 seconds
    }

    // === INTEGRATION AND CONSISTENCY TESTS (601+) ===

    @Test
    @Order(601)
    void generate_multipleInstancesWithSameLength_shouldProduceDifferentResults() {
        PasswordGeneratorService service1 = new PasswordGeneratorService();
        PasswordGeneratorService service2 = new PasswordGeneratorService();

        String password1 = service1.generate(20);
        String password2 = service2.generate(20);

        // Different instances should produce different passwords
        assertThat(password1).isNotEqualTo(password2);
    }

    @Test
    @Order(602)
    void generate_consistentBehaviorAcrossMultipleCalls() {
        // Test that the service behaves consistently
        for (int length = 1; length <= 50; length++) {
            String password = service.generate(length);

            assertThat(password)
                    .hasSize(length)
                    .matches("[A-Za-z0-9]*");
        }
    }
}
