package dev.ilkersahin.java.spring.gym.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PasswordGeneratorServiceTest {

    private final PasswordGeneratorService service = new PasswordGeneratorService();

    @Test
    void generatesCorrectLength() {
        String pw = service.generate(10);

        assertThat(pw).hasSize(10);
    }

    @Test
    void usesOnlyAllowedCharacters() {
        String pw = service.generate(100);

        assertThat(pw).matches("[A-Za-z0-9]+");
    }

    @Test
    void twoPasswordsAreNotTheSameMostOfTheTime() {
        String a = service.generate(16);
        String b = service.generate(16);

        assertThat(a).isNotEqualTo(b);
    }
}
