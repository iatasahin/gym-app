package dev.ilkersahin.java.spring.gym.service.util;

import dev.ilkersahin.java.spring.gym.exception.MaxUsernameSuffixRetriesExceededException;
import dev.ilkersahin.java.spring.gym.exception.UsernameExistsException;
import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserCreationServiceTest {
    private UserCreationService service;
    private PasswordGeneratorService passwordGeneratorService;

    @BeforeEach
    void setUp(){
        passwordGeneratorService = mock(PasswordGeneratorService.class);
        service = new UserCreationService(passwordGeneratorService);
        service.setMaxSuffixRetriesForUsername(1000); // default value in application.properties
    }

    private Trainer createSampleTrainer() {
        Trainer trainer = new Trainer();
        trainer.setFirstName("Tom");
        trainer.setLastName("Smith");
        trainer.setSpecialization(TrainingType.FITNESS);
        trainer.setUserId(UUID.randomUUID());
        trainer.setActive(true);
        return trainer;
    }

    private Trainer createSampleTrainer(String firstName, String lastName) {
        Trainer trainer = new Trainer();
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setSpecialization(TrainingType.FITNESS);
        trainer.setUserId(UUID.randomUUID());
        trainer.setActive(true);
        return trainer;
    }

    private Trainee createSampleTrainee() {
        Trainee trainee = new Trainee();
        trainee.setFirstName("Jack");
        trainee.setLastName("Black");
        trainee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        trainee.setAddress("123 Main Street");
        trainee.setUserId(UUID.randomUUID());
        trainee.setActive(true);
        return trainee;
    }

    private Trainee createSampleTrainee(String firstName, String lastName) {
        Trainee trainee = new Trainee();
        trainee.setFirstName(firstName);
        trainee.setLastName(lastName);
        trainee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        trainee.setAddress("123 Main Street");
        trainee.setUserId(UUID.randomUUID());
        trainee.setActive(true);
        return trainee;
    }

    // === CREATE TRAINER TESTS ===

    @Test
    @Order(101)
    void createUser_withValidTrainer_shouldGeneratePasswordAndDefaultUsername() {
        Trainer trainer = createSampleTrainer();
        Function<Trainer, Trainer> persistFunction = Function.identity();

        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");

        Trainer result = service.createUser(trainer, persistFunction, "Trainer");

        assertThat(result.getPassword()).isEqualTo("secretPass");
        assertThat(result.getUsername()).isEqualTo("Tom.Smith");
        assertThat(result.getSpecialization()).isEqualTo(TrainingType.FITNESS);
        assertThat(result.isActive()).isTrue();
        verify(passwordGeneratorService).generate(10);
    }

    @Test
    @Order(102)
    void createUser_withTrainerWithDifferentSpecialization_shouldPreserveSpecialization() {
        Trainer trainer = createSampleTrainer();
        trainer.setSpecialization(TrainingType.YOGA);
        Function<Trainer, Trainer> persistFunction = Function.identity();

        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");

        Trainer result = service.createUser(trainer, persistFunction, "Trainer");

        assertThat(result.getSpecialization()).isEqualTo(TrainingType.YOGA);
        assertThat(result.getUsername()).isEqualTo("Tom.Smith");
        assertThat(result.getPassword()).isEqualTo("secretPass");
    }

    @Test
    @Order(103)
    void createUser_withTrainerWithSpecialCharactersInName_shouldGenerateValidUsername() {
        Trainer trainer = createSampleTrainer("Tom-John", "O'Smith");
        trainer.setSpecialization(TrainingType.RESISTANCE);
        Function<Trainer, Trainer> persistFunction = Function.identity();

        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");

        Trainer result = service.createUser(trainer, persistFunction, "Trainer");

        assertThat(result.getUsername()).isEqualTo("Tom-John.O'Smith");
        assertThat(result.getSpecialization()).isEqualTo(TrainingType.RESISTANCE);
    }

    @Test
    @Order(104)
    void createUser_withAllTrainingTypes_shouldPreserveSpecialization() {
        TrainingType[] types = {TrainingType.FITNESS, TrainingType.YOGA, TrainingType.ZUMBA,
                TrainingType.STRETCHING, TrainingType.RESISTANCE};

        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");

        for (TrainingType type : types) {
            Trainer trainer = createSampleTrainer("Test", type.getTrainingTypeName());
            trainer.setSpecialization(type);
            Function<Trainer, Trainer> persistFunction = Function.identity();

            Trainer result = service.createUser(trainer, persistFunction, "Trainer");

            assertThat(result.getSpecialization()).isEqualTo(type);
            assertThat(result.getUsername()).isEqualTo("Test." + type.getTrainingTypeName());
        }
    }

    @Test
    @Order(105)
    void createUser_withInactiveTrainer_shouldPreserveActiveStatus() {
        Trainer trainer = createSampleTrainer();
        trainer.setActive(false);
        Function<Trainer, Trainer> persistFunction = Function.identity();

        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");

        Trainer result = service.createUser(trainer, persistFunction, "Trainer");

        assertThat(result.isActive()).isFalse();
        assertThat(result.getUsername()).isEqualTo("Tom.Smith");
    }

    // === CREATE TRAINEE TESTS ===

    @Test
    @Order(151)
    void createUser_withValidTrainee_shouldGeneratePasswordAndDefaultUsername() {
        Trainee trainee = createSampleTrainee();
        Function<Trainee, Trainee> persistFunction = Function.identity();

        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");

        Trainee result = service.createUser(trainee, persistFunction, "Trainee");

        assertThat(result.getPassword()).isEqualTo("secretPass");
        assertThat(result.getUsername()).isEqualTo("Jack.Black");
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(result.getAddress()).isEqualTo("123 Main Street");
        assertThat(result.isActive()).isTrue();
        verify(passwordGeneratorService).generate(10);
    }

    @Test
    @Order(152)
    void createUser_withTraineeWithDifferentDetails_shouldPreserveDetails() {
        Trainee trainee = createSampleTrainee("Alice", "Johnson");
        trainee.setDateOfBirth(LocalDate.of(1985, 5, 15));
        trainee.setAddress("456 Oak Avenue");
        Function<Trainee, Trainee> persistFunction = Function.identity();

        when(passwordGeneratorService.generate(10)).thenReturn("password456");

        Trainee result = service.createUser(trainee, persistFunction, "Trainee");

        assertThat(result.getUsername()).isEqualTo("Alice.Johnson");
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1985, 5, 15));
        assertThat(result.getAddress()).isEqualTo("456 Oak Avenue");
        assertThat(result.getPassword()).isEqualTo("password456");
    }

    @Test
    @Order(153)
    void createUser_withTraineeWithNullAddress_shouldHandleGracefully() {
        Trainee trainee = createSampleTrainee();
        trainee.setAddress(null);
        Function<Trainee, Trainee> persistFunction = Function.identity();

        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");

        Trainee result = service.createUser(trainee, persistFunction, "Trainee");

        assertThat(result.getUsername()).isEqualTo("Jack.Black");
        assertThat(result.getAddress()).isNull();
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    @Order(154)
    void createUser_withTraineeWithSpecialCharactersInName_shouldGenerateValidUsername() {
        Trainee trainee = createSampleTrainee("Mary-Jane", "O'Connor");
        Function<Trainee, Trainee> persistFunction = Function.identity();

        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");

        Trainee result = service.createUser(trainee, persistFunction, "Trainee");

        assertThat(result.getUsername()).isEqualTo("Mary-Jane.O'Connor");
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    @Order(155)
    void createUser_withInactiveTrainee_shouldPreserveActiveStatus() {
        Trainee trainee = createSampleTrainee();
        trainee.setActive(false);
        Function<Trainee, Trainee> persistFunction = Function.identity();

        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");

        Trainee result = service.createUser(trainee, persistFunction, "Trainee");

        assertThat(result.isActive()).isFalse();
        assertThat(result.getUsername()).isEqualTo("Jack.Black");
    }

    // === USERNAME COLLISION HANDLING TESTS ===

    @Test
    @Order(201)
    void createUser_withTrainerUsernameCollision_shouldAppendSerialNumber() {
        Trainer trainer = createSampleTrainer();
        Function<Trainer, Trainer> persistFunction = mock(Function.class);

        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");
        when(persistFunction.apply(any(Trainer.class)))
                .thenThrow(new UsernameExistsException("Username already exists"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Trainer result = service.createUser(trainer, persistFunction, "Trainer");

        assertThat(result.getUsername()).isEqualTo("Tom.Smith2");
        assertThat(result.getPassword()).isEqualTo("secretPass");
        assertThat(result.getSpecialization()).isEqualTo(TrainingType.FITNESS);
        verify(persistFunction, times(2)).apply(trainer);
    }

    @Test
    @Order(202)
    void createUser_withTraineeUsernameCollision_shouldAppendSerialNumber() {
        Trainee trainee = createSampleTrainee();
        Function<Trainee, Trainee> persistFunction = mock(Function.class);

        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");
        when(persistFunction.apply(trainee))
                .thenThrow(new UsernameExistsException("Username already exists"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Trainee result = service.createUser(trainee, persistFunction, "Trainee");

        assertThat(result.getUsername()).isEqualTo("Jack.Black2");
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
        verify(persistFunction, times(2)).apply(trainee);
    }

    @Test
    @Order(203)
    void createUser_withMultipleUsernameCollisions_shouldIncrementSerialNumber() {
        Trainer trainer = createSampleTrainer();
        Function<Trainer, Trainer> persistFunction = mock(Function.class);

        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");
        when(persistFunction.apply(any(Trainer.class)))
                .thenThrow(new UsernameExistsException("Username already exists"))
                .thenThrow(new UsernameExistsException("Username already exists"))
                .thenThrow(new UsernameExistsException("Username already exists"))
                .thenReturn(trainer);

        Trainer result = service.createUser(trainer, persistFunction, "Trainer");

        assertThat(result.getUsername()).isEqualTo("Tom.Smith4");
        verify(persistFunction, times(4)).apply(any(Trainer.class));
    }

    @Test
    @Order(204)
    void createUser_withMaxRetriesExceeded_shouldThrowMaxUsernameSuffixRetriesExceededException() {
        service.setMaxSuffixRetriesForUsername(3);
        Trainee trainee = createSampleTrainee();
        Function<Trainee, Trainee> persistFunction = mock(Function.class);

        when(passwordGeneratorService.generate(10)).thenReturn("password123");
        when(persistFunction.apply(any(Trainee.class)))
                .thenThrow(new UsernameExistsException("Username already exists"));

        assertThatThrownBy(() -> service.createUser(trainee, persistFunction, "Trainee"))
                .isInstanceOf(MaxUsernameSuffixRetriesExceededException.class)
                .hasMessageContaining("Maximum username retries for Trainee with Username 'Jack.Black' exceeded the maximum of '3'");

        // Should try: Jack.Black, Jack.Black2, Jack.Black3, Jack.Black4 (4 attempts total)
        verify(persistFunction, times(4)).apply(trainee); // Initial + 3 retries
    }

    @Test
    @Order(205)
    void createUser_withMaxRetriesSetToOne_shouldFailAfterTwoAttempts() {
        Trainer trainer = createSampleTrainer();
        Function<Trainer, Trainer> persistFunction = mock(Function.class);
        service.setMaxSuffixRetriesForUsername(1);

        when(passwordGeneratorService.generate(10)).thenReturn("password123");
        when(persistFunction.apply(any(Trainer.class)))
                .thenThrow(new UsernameExistsException("Username already exists"));

        assertThatThrownBy(() -> service.createUser(trainer, persistFunction, "Trainer"))
                .isInstanceOf(MaxUsernameSuffixRetriesExceededException.class)
                .hasMessageContaining("Maximum username retries for Trainer with Username 'Tom.Smith' exceeded the maximum of '1'");

        // Should try: Tom.Smith, Tom.Smith2 (2 attempts total)
        verify(persistFunction, times(2)).apply(trainer);
    }

    @Test
    @Order(206)
    void createUser_withMaxRetriesSetToZero_shouldFailAfterFirstAttempt() {
        Trainee trainee = createSampleTrainee();
        Function<Trainee, Trainee> persistFunction = mock(Function.class);
        service.setMaxSuffixRetriesForUsername(0);

        when(passwordGeneratorService.generate(10)).thenReturn("password123");
        when(persistFunction.apply(trainee))
                .thenThrow(new UsernameExistsException("Username already exists"));

        assertThatThrownBy(() -> service.createUser(trainee, persistFunction, "Trainee"))
                .isInstanceOf(MaxUsernameSuffixRetriesExceededException.class)
                .hasMessageContaining("Maximum username retries for Trainee with Username 'Jack.Black' exceeded the maximum of '0'");

        // Should try only: Jack.Black (1 attempt total)
        verify(persistFunction, times(1)).apply(trainee);
    }

    @Test
    @Order(207)
    void createUser_withSuccessOnLastAllowedAttempt_shouldSucceed() {
        Trainer trainer = createSampleTrainer();
        Function<Trainer, Trainer> persistFunction = mock(Function.class);
        service.setMaxSuffixRetriesForUsername(3);

        when(passwordGeneratorService.generate(10)).thenReturn("password123");
        when(persistFunction.apply(trainer))
                .thenThrow(new UsernameExistsException("Username already exists"))
                .thenThrow(new UsernameExistsException("Username already exists"))
                .thenThrow(new UsernameExistsException("Username already exists"))
                .thenReturn(trainer); // Success on 4th attempt (Tom.Smith4)

        Trainer result = service.createUser(trainer, persistFunction, "Trainer");

        assertThat(result.getUsername()).isEqualTo("Tom.Smith4");
        verify(persistFunction, times(4)).apply(trainer);
    }

    @Test
    @Order(208)
    void createUser_withMaxRetriesExceededForSpecialCharacterNames_shouldThrowWithCorrectDefaultUsername() {
        Trainer trainer = createSampleTrainer("John-Paul", "O'Connor");
        Function<Trainer, Trainer> persistFunction = mock(Function.class);
        service.setMaxSuffixRetriesForUsername(2);

        when(passwordGeneratorService.generate(10)).thenReturn("password123");
        when(persistFunction.apply(trainer))
                .thenThrow(new UsernameExistsException("Username already exists"));

        assertThatThrownBy(() -> service.createUser(trainer, persistFunction, "Trainer"))
                .isInstanceOf(MaxUsernameSuffixRetriesExceededException.class)
                .hasMessageContaining("Maximum username retries for Trainer with Username 'John-Paul.O'Connor' exceeded the maximum of '2'");

        // Should try: John-Paul.O'Connor, John-Paul.O'Connor2, John-Paul.O'Connor3 (3 attempts total)
        verify(persistFunction, times(3)).apply(trainer);
    }


    // === EDGE CASES AND ERROR HANDLING TESTS ===

    @Test
    @Order(301)
    void createUser_withNullUser_shouldThrowException() {
        Function<Trainer, Trainer> persistFunction = Function.identity();

        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");

        assertThatThrownBy(() -> service.createUser(null, persistFunction, "Trainer"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @Order(302)
    void createUser_withNullPersistFunction_shouldThrowException() {
        Trainer trainer = createSampleTrainer();

        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");

        assertThatThrownBy(() -> service.createUser(trainer, null, "Trainer"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @Order(303)
    void createUser_withNullUserType_shouldHandleGracefully() {
        Trainer trainer = createSampleTrainer();
        Function<Trainer, Trainer> persistFunction = Function.identity();

        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");

        Trainer result = service.createUser(trainer, persistFunction, null);

        assertThat(result.getUsername()).isEqualTo("Tom.Smith");
        assertThat(result.getPassword()).isEqualTo("secretPass");
    }

    @Test
    @Order(304)
    void createUser_withEmptyUserType_shouldHandleGracefully() {
        Trainer trainer = createSampleTrainer();
        Function<Trainer, Trainer> persistFunction = Function.identity();

        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");

        Trainer result = service.createUser(trainer, persistFunction, "");

        assertThat(result.getUsername()).isEqualTo("Tom.Smith");
        assertThat(result.getPassword()).isEqualTo("secretPass");
    }

    @Test
    @Order(305)
    void createUser_withPasswordGeneratorException_shouldPropagateException() {
        Trainer trainer = createSampleTrainer();
        Function<Trainer, Trainer> persistFunction = Function.identity();

        when(passwordGeneratorService.generate(10))
                .thenThrow(new RuntimeException("Password generation failed"));

        assertThatThrownBy(() -> service.createUser(trainer, persistFunction, "Trainer"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Password generation failed");
    }

    // === CONFIGURATION TESTS  ===

    @Test
    @Order(401)
    void setMaxUsernameSuffixRetriesForUsername_withValidValue_shouldUpdateConfiguration() {
        service.setMaxSuffixRetriesForUsername(5);
        Trainer trainer = createSampleTrainer();
        Function<Trainer, Trainer> persistFunction = mock(Function.class);

        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");
        when(persistFunction.apply(any(Trainer.class)))
                .thenThrow(new UsernameExistsException("Username already exists"));

        assertThatThrownBy(() -> service.createUser(trainer, persistFunction, "Trainer"))
                .isInstanceOf(MaxUsernameSuffixRetriesExceededException.class)
                .hasMessageContaining("exceeded the maximum of '5'");

        verify(persistFunction, times(6)).apply(any(Trainer.class)); // Initial + 5 retries
    }

    @Test
    @Order(402)
    void setMaxUsernameSuffixRetriesForUsername_withZeroValue_shouldAllowNoRetries() {
        service.setMaxSuffixRetriesForUsername(0);
        Trainer trainer = createSampleTrainer();
        Function<Trainer, Trainer> persistFunction = mock(Function.class);

        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");
        when(persistFunction.apply(any(Trainer.class)))
                .thenThrow(new UsernameExistsException("Username already exists"));

        assertThatThrownBy(() -> service.createUser(trainer, persistFunction, "Trainer"))
                .isInstanceOf(MaxUsernameSuffixRetriesExceededException.class)
                .hasMessageContaining("exceeded the maximum of '0'");

        verify(persistFunction, times(1)).apply(any(Trainer.class)); // Only initial attempt
    }

    @Test
    @Order(403)
    void setMaxUsernameSuffixRetriesForUsername_withNegativeValue_shouldHandleGracefully() {
        service.setMaxSuffixRetriesForUsername(-1);
        Trainer trainer = createSampleTrainer();
        Function<Trainer, Trainer> persistFunction = mock(Function.class);

        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");
        when(persistFunction.apply(any(Trainer.class)))
                .thenThrow(new UsernameExistsException("Username already exists"));

        assertThatThrownBy(() -> service.createUser(trainer, persistFunction, "Trainer"))
                .isInstanceOf(MaxUsernameSuffixRetriesExceededException.class);
    }



    @Test
    @Order(601)
    void createUser_multipleUsersWithSameBaseName_shouldGenerateUniqueUsernames() {
        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");

        // Create multiple trainers with same name but different persist functions
        Trainer trainer1 = createSampleTrainer("Same", "Name");
        Trainer trainer2 = createSampleTrainer("Same", "Name");
        Trainer trainer3 = createSampleTrainer("Same", "Name");

        Function<Trainer, Trainer> persistFunction1 = Function.identity();
        Function<Trainer, Trainer> persistFunction2 = mock(Function.class);
        Function<Trainer, Trainer> persistFunction3 = mock(Function.class);

        // Second trainer encounters collision
        when(persistFunction2.apply(any(Trainer.class)))
                .thenThrow(new UsernameExistsException("Username already exists"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Third trainer encounters multiple collisions
        when(persistFunction3.apply(any(Trainer.class)))
                .thenThrow(new UsernameExistsException("Username already exists"))
                .thenThrow(new UsernameExistsException("Username already exists"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Trainer result1 = service.createUser(trainer1, persistFunction1, "Trainer");
        Trainer result2 = service.createUser(trainer2, persistFunction2, "Trainer");
        Trainer result3 = service.createUser(trainer3, persistFunction3, "Trainer");

        assertThat(result1.getUsername()).isEqualTo("Same.Name");
        assertThat(result2.getUsername()).isEqualTo("Same.Name2");
        assertThat(result3.getUsername()).isEqualTo("Same.Name3");
    }

    @Test
    @Order(602)
    void createUser_withDifferentPasswordLengths_shouldUseCorrectLength() {
        Trainer trainer = createSampleTrainer();
        Function<Trainer, Trainer> persistFunction = Function.identity();

        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");

        service.createUser(trainer, persistFunction, "Trainer");

        verify(passwordGeneratorService).generate(10);
        verifyNoMoreInteractions(passwordGeneratorService);
    }
}
