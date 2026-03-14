package dev.ilkersahin.java.spring.gym.service.util;

import dev.ilkersahin.java.spring.gym.model.util.UsernameCounter;
import dev.ilkersahin.java.spring.gym.repository.UserRepository;
import dev.ilkersahin.java.spring.gym.repository.util.UsernameCounterRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UsernameGeneratorServiceTest {
    @Mock
    private UsernameCounterRepository usernameCounterRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UsernameGeneratorService usernameGeneratorService;

    // =========================================================================
    // GENERATE UNIQUE USERNAME - BASE USERNAME AVAILABLE (100s)
    // =========================================================================

    @Test
    @Order(101)
    void generateUniqueUsername_withAvailableBaseUsername_shouldReturnBaseUsername() {
        when(userRepository.existsByUsername("Jack.Black")).thenReturn(false);

        String username = usernameGeneratorService.generateUniqueUsername("Jack", "Black");

        assertThat(username).isEqualTo("Jack.Black");
    }

    @Test
    @Order(102)
    void generateUniqueUsername_withAvailableBaseUsername_shouldCreateCounterEntry() {
        when(userRepository.existsByUsername("Jack.Black")).thenReturn(false);

        usernameGeneratorService.generateUniqueUsername("Jack", "Black");

        ArgumentCaptor<UsernameCounter> captor = ArgumentCaptor.forClass(UsernameCounter.class);
        verify(usernameCounterRepository).saveAndFlush(captor.capture());

        assertThat(captor.getValue().getBaseUsername()).isEqualTo("Jack.Black");
    }

    // =========================================================================
    // GENERATE UNIQUE USERNAME - WITH SUFFIX, COUNTER EXISTS (200s)
    // =========================================================================

    @Test
    @Order(201)
    void generateUniqueUsername_withExistingUsernameAndCounter_shouldReturnUsernameWithSuffix() {
        when(userRepository.existsByUsername("Jack.Black")).thenReturn(true);

        UsernameCounter counter = new UsernameCounter("Jack.Black");
        when(usernameCounterRepository.findByBaseUsernameWithLock("Jack.Black")).thenReturn(Optional.of(counter));

        String username = usernameGeneratorService.generateUniqueUsername("Jack", "Black");

        assertThat(username).isEqualTo("Jack.Black2");
    }

    @Test
    @Order(202)
    void generateUniqueUsername_withExistingUsernameAndCounter_shouldIncrementCounter() {
        when(userRepository.existsByUsername("Jack.Black")).thenReturn(true);

        UsernameCounter counter = new UsernameCounter("Jack.Black");
        when(usernameCounterRepository.findByBaseUsernameWithLock("Jack.Black")).thenReturn(Optional.of(counter));

        usernameGeneratorService.generateUniqueUsername("Jack", "Black");

        verify(usernameCounterRepository).save(counter);
        assertThat(counter.getCurrentSuffix()).isEqualTo(3);
    }

    @Test
    @Order(203)
    void generateUniqueUsername_calledMultipleTimes_shouldIncrementSuffix() {
        when(userRepository.existsByUsername("Jack.Black")).thenReturn(true);

        UsernameCounter counter = new UsernameCounter("Jack.Black");
        when(usernameCounterRepository.findByBaseUsernameWithLock("Jack.Black")).thenReturn(Optional.of(counter));

        String username1 = usernameGeneratorService.generateUniqueUsername("Jack", "Black");
        String username2 = usernameGeneratorService.generateUniqueUsername("Jack", "Black");
        String username3 = usernameGeneratorService.generateUniqueUsername("Jack", "Black");

        assertThat(username1).isEqualTo("Jack.Black2");
        assertThat(username2).isEqualTo("Jack.Black3");
        assertThat(username3).isEqualTo("Jack.Black4");
    }

    // =========================================================================
    // GENERATE UNIQUE USERNAME - WITH SUFFIX, COUNTER DOES NOT EXIST (300s)
    // =========================================================================

    @Test
    @Order(301)
    void generateUniqueUsername_withExistingUsernameButNoCounter_shouldCreateCounterAndReturnWithSuffix() {
        when(userRepository.existsByUsername("Jack.Black")).thenReturn(true);
        when(usernameCounterRepository.findByBaseUsernameWithLock("Jack.Black")).thenReturn(Optional.empty());
        when(usernameCounterRepository.saveAndFlush(any(UsernameCounter.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String username = usernameGeneratorService.generateUniqueUsername("Jack", "Black");

        assertThat(username).isEqualTo("Jack.Black2");
    }

    @Test
    @Order(302)
    void generateUniqueUsername_withExistingUsernameButNoCounter_shouldPersistNewCounter() {
        when(userRepository.existsByUsername("Jack.Black")).thenReturn(true);
        when(usernameCounterRepository.findByBaseUsernameWithLock("Jack.Black")).thenReturn(Optional.empty());
        when(usernameCounterRepository.saveAndFlush(any(UsernameCounter.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        usernameGeneratorService.generateUniqueUsername("Jack", "Black");

        ArgumentCaptor<UsernameCounter> captor = ArgumentCaptor.forClass(UsernameCounter.class);
        verify(usernameCounterRepository).saveAndFlush(captor.capture());

        assertThat(captor.getValue().getBaseUsername()).isEqualTo("Jack.Black");
    }

    // =========================================================================
    // DIFFERENT NAMES (400s)
    // =========================================================================

    @Test
    @Order(401)
    void generateUniqueUsername_withDifferentNames_shouldConcatenateCorrectly() {
        when(userRepository.existsByUsername("Alice.Smith")).thenReturn(false);

        String username = usernameGeneratorService.generateUniqueUsername("Alice", "Smith");

        assertThat(username).isEqualTo("Alice.Smith");
    }

}
