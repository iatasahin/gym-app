package dev.ilkersahin.java.spring.gym.service;

import dev.ilkersahin.java.spring.gym.dao.TraineeDAO;
import dev.ilkersahin.java.spring.gym.exception.UsernameExistsException;
import dev.ilkersahin.java.spring.gym.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TraineeServiceTest {
    private TraineeService service;
    private TraineeDAO traineeDAO;
    private PasswordGeneratorService passwordService;

    @BeforeEach
    void setUp() {
        traineeDAO = mock(TraineeDAO.class);
        passwordService = mock(PasswordGeneratorService.class);

        service = new TraineeService();
        service.setTraineeDAO(traineeDAO);
        service.setPasswordGeneratorService(passwordService);
    }

    private Trainee sample() {
        Trainee t = new Trainee();
        t.setFirstName("Jack");
        t.setLastName("Black");
        t.setDateOfBirth(LocalDate.of(1990,1,1));
        t.setUserId(UUID.randomUUID());
        return t;
    }

    @Test
    void createsTraineeWithGeneratedPasswordAndDefaultUsername() {
        Trainee t = sample();

        when(passwordService.generate(10)).thenReturn("secretPass");
        when(traineeDAO.createTrainee(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        Trainee saved = service.createTrainee(t);

        assertThat(saved.getPassword()).isEqualTo("secretPass");
        assertThat(saved.getUsername()).isEqualTo("Jack.Black");
    }


    @Test
    void appendsNumberWhenUsernameExists() {
        Trainee t = sample();

        when(passwordService.generate(10)).thenReturn("secretPass");

        when(traineeDAO.createTrainee(any()))
                .thenThrow(new UsernameExistsException(""))
                .thenAnswer(inv -> inv.getArgument(0));

        Trainee saved = service.createTrainee(t);

        assertThat(saved.getUsername()).isEqualTo("Jack.Black2");
    }

    @Test
    void retriesUntilUniqueUsernameFound() {
        Trainee t = sample();

        when(passwordService.generate(10)).thenReturn("pw");

        when(traineeDAO.createTrainee(any()))
                .thenThrow(new UsernameExistsException(""))
                .thenThrow(new UsernameExistsException(""))
                .thenAnswer(inv -> inv.getArgument(0));

        Trainee saved = service.createTrainee(t);

        assertThat(saved.getUsername()).isEqualTo("Jack.Black3");
        verify(traineeDAO, times(3)).createTrainee(any());
    }

    @Test
    void updateTraineeDelegatesToDao() {
        Trainee t = sample();
        t.setUsername("Jack.Black");

        when(traineeDAO.updateTrainee(t)).thenReturn(t);

        Trainee updated = service.updateTrainee(t);

        assertThat(updated).isSameAs(t);
        verify(traineeDAO).updateTrainee(t);
    }

    @Test
    void deleteTraineeDelegatesToDao() {
        Trainee t = sample();
        t.setUsername("Jack.Black");

        when(traineeDAO.deleteTrainee("Jack.Black"))
                .thenReturn(Optional.of(t));

        Optional<Trainee> result = service.deleteTrainee("Jack.Black");

        assertThat(result).contains(t);
        verify(traineeDAO).deleteTrainee("Jack.Black");
    }

    @Test
    void deleteTraineeReturnsEmptyWhenNotFound() {
        when(traineeDAO.deleteTrainee("missing"))
                .thenReturn(Optional.empty());

        Optional<Trainee> result = service.deleteTrainee("missing");

        assertThat(result).isEmpty();
    }

    @Test
    void getTraineeDelegatesToDao() {
        Trainee t = sample();
        t.setUsername("Jack.Black");

        when(traineeDAO.getTrainee("Jack.Black"))
                .thenReturn(Optional.of(t));

        Optional<Trainee> result = service.getTrainee("Jack.Black");

        assertThat(result).contains(t);
        verify(traineeDAO).getTrainee("Jack.Black");
    }

    @Test
    void getTraineeReturnsEmptyWhenMissing() {
        when(traineeDAO.getTrainee("missing"))
                .thenReturn(Optional.empty());

        assertThat(service.getTrainee("missing")).isEmpty();
    }

    @Test
    void getAllTraineesDelegatesToDao() {
        Trainee t1 = sample();
        Trainee t2 = sample();

        when(traineeDAO.getAllTrainees())
                .thenReturn(List.of(t1, t2));

        List<Trainee> result = service.getAllTrainees();

        assertThat(result)
                .hasSize(2)
                .containsExactly(t1, t2);

        verify(traineeDAO).getAllTrainees();
    }
}
