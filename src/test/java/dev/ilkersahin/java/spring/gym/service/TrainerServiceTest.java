package dev.ilkersahin.java.spring.gym.service;


import dev.ilkersahin.java.spring.gym.dao.TrainerDAO;
import dev.ilkersahin.java.spring.gym.exception.UsernameExistsException;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import dev.ilkersahin.java.spring.gym.service.util.PasswordGeneratorService;
import dev.ilkersahin.java.spring.gym.service.util.UserCreationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TrainerServiceTest {

    private TrainerService service;
    private TrainerDAO trainerDAO;
    private PasswordGeneratorService passwordService;
    private UserCreationService userCreationService;

    @BeforeEach
    void setUp() {
        trainerDAO = mock(TrainerDAO.class);
        passwordService = mock(PasswordGeneratorService.class);

        userCreationService = new UserCreationService(passwordService);
        userCreationService.setMaxSuffixRetriesForUsername(1000); // default value in application.properties

        service = new TrainerService();
        service.setTrainerDAO(trainerDAO);
        service.setUserCreationService(userCreationService);
    }

    private Trainer sample() {
        Trainer t = new Trainer();
        t.setFirstName("Tom");
        t.setLastName("Smith");
        t.setSpecialization(TrainingType.FITNESS);
        t.setUserId(UUID.randomUUID());
        return t;
    }

    @Test
    void generatesPasswordAndDefaultUsername() {
        Trainer t = sample();

        when(passwordService.generate(10)).thenReturn("secretPass");
        when(trainerDAO.createTrainer(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        Trainer saved = service.createTrainer(t);

        assertThat(saved.getPassword()).isEqualTo("secretPass");
        assertThat(saved.getUsername()).isEqualTo("Tom.Smith");
    }

    @Test
    void handlesDuplicateUsername() {
        Trainer t = sample();

        when(passwordService.generate(10)).thenReturn("secretPass");

        when(trainerDAO.createTrainer(any()))
                .thenThrow(new UsernameExistsException(""))
                .thenAnswer(inv -> inv.getArgument(0));

        Trainer saved = service.createTrainer(t);

        assertThat(saved.getUsername()).isEqualTo("Tom.Smith2");
    }

    @Test
    void updateTrainerDelegatesToDao() {
        Trainer t = sample();
        t.setUsername("Tom.Smith");

        when(trainerDAO.updateTrainer(t)).thenReturn(t);

        Trainer updated = service.updateTrainer(t);

        assertThat(updated).isSameAs(t);
        verify(trainerDAO).updateTrainer(t);
    }

    @Test
    void getTrainerDelegatesToDao() {
        Trainer t = sample();
        t.setUsername("Tom.Smith");

        when(trainerDAO.getTrainer("Tom.Smith"))
                .thenReturn(Optional.of(t));

        Optional<Trainer> result = service.getTrainer("Tom.Smith");

        assertThat(result).contains(t);
        verify(trainerDAO).getTrainer("Tom.Smith");
    }

    @Test
    void getTrainerReturnsEmptyWhenMissing() {
        when(trainerDAO.getTrainer("missing"))
                .thenReturn(Optional.empty());

        assertThat(service.getTrainer("missing")).isEmpty();
    }

    @Test
    void getAllTrainersDelegatesToDao() {
        Trainer t1 = sample();
        Trainer t2 = sample();

        when(trainerDAO.getAllTrainers())
                .thenReturn(List.of(t1, t2));

        List<Trainer> result = service.getAllTrainers();

        assertThat(result)
                .hasSize(2)
                .containsExactly(t1, t2);

        verify(trainerDAO).getAllTrainers();
    }
}
