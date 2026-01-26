package dev.ilkersahin.java.spring.gym.repository.util;


import dev.ilkersahin.java.spring.gym.config.TestPersistenceConfig;
import dev.ilkersahin.java.spring.gym.dao.util.UsernameCounterDao;
import dev.ilkersahin.java.spring.gym.model.util.UsernameCounter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestPersistenceConfig.class)
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UsernameCounterRepositoryImplTest {
    @Autowired
    private UsernameCounterDao usernameCounterRepository;

    private UsernameCounter counter;

    @BeforeEach
    void setUp() {
        counter = new UsernameCounter("Jack.Black");
    }

    // =========================================================================
    // PERSIST TESTS (100s)
    // =========================================================================

    @Test
    @Order(101)
    void persist_withValidCounter_shouldPersist() {
        usernameCounterRepository.persist(counter);

        UsernameCounter found = usernameCounterRepository.findByBaseUsernameWithLock("Jack.Black");

        assertThat(found).isNotNull();
        assertThat(found.getBaseUsername()).isEqualTo("Jack.Black");
    }

    @Test
    @Order(102)
    void persist_withValidCounter_shouldHaveInitialSuffix() {
        usernameCounterRepository.persist(counter);

        UsernameCounter found = usernameCounterRepository.findByBaseUsernameWithLock("Jack.Black");

        assertThat(found.getCurrentSuffix()).isEqualTo(2);
    }

    // =========================================================================
    // FIND BY BASE USERNAME WITH LOCK TESTS (200s)
    // =========================================================================

    @Test
    @Order(201)
    void findByBaseUsernameWithLock_withExistingUsername_shouldReturnCounter() {
        usernameCounterRepository.persist(counter);

        UsernameCounter found = usernameCounterRepository.findByBaseUsernameWithLock("Jack.Black");

        assertThat(found).isNotNull();
        assertThat(found.getBaseUsername()).isEqualTo("Jack.Black");
    }

    @Test
    @Order(202)
    void findByBaseUsernameWithLock_withNonExistingUsername_shouldReturnNull() {
        UsernameCounter found = usernameCounterRepository.findByBaseUsernameWithLock("Non.Existent");

        assertThat(found).isNull();
    }

    // =========================================================================
    // MERGE TESTS (300s)
    // =========================================================================

    @Test
    @Order(301)
    void merge_withExistingCounter_shouldUpdateCounter() {
        usernameCounterRepository.persist(counter);

        counter.getAndIncrementSuffix();
        counter.getAndIncrementSuffix();

        UsernameCounter merged = usernameCounterRepository.merge(counter);

        assertThat(merged.getCurrentSuffix()).isEqualTo(4);
    }
}
