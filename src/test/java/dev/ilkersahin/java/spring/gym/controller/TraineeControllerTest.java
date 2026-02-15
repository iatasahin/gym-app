package dev.ilkersahin.java.spring.gym.controller;

import dev.ilkersahin.java.spring.gym.dto.request.*;
import dev.ilkersahin.java.spring.gym.dto.response.ActivationResponse;
import dev.ilkersahin.java.spring.gym.dto.response.UserCreateResponse;
import dev.ilkersahin.java.spring.gym.dto.view.TraineeWithListView;
import dev.ilkersahin.java.spring.gym.dto.view.TrainerInfo;
import dev.ilkersahin.java.spring.gym.dto.view.TrainingView;
import dev.ilkersahin.java.spring.gym.exception.UserAlreadyActiveException;
import dev.ilkersahin.java.spring.gym.exception.UserAlreadyInactiveException;
import dev.ilkersahin.java.spring.gym.service.TraineeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TraineeControllerTest {

    @Mock private TraineeService traineeService;

    @Spy
    @InjectMocks
    private TraineeController traineeController;


    private TraineeCreateRequest createRequest;
    private TraineeUpdateRequest updateRequest;
    private UserCreateResponse createResponse;
    private TraineeWithListView activeTraineeView;
    private TraineeWithListView inactiveTraineeView;
    private TrainerInfo trainerInfo;
    private TrainingView trainingView;

    @BeforeEach
    void setUp() {
        // Stub verifyUserAccess to do nothing by default (bypass authentication)
        lenient().doNothing().when(traineeController).verifyUserAccess(any());

        createRequest = new TraineeCreateRequest(
                "John", "Doe", LocalDate.of(1990, 1, 1), "123 Main St"
        );

        updateRequest = new TraineeUpdateRequest(
                "John.Doe", "John", "Smith", true,
                LocalDate.of(1990, 1, 1), "456 Oak St"
        );

        createResponse = new UserCreateResponse("John.Doe", "password123");

        activeTraineeView = new TraineeWithListView(
                "John.Doe", "John", "Doe", true,
                LocalDate.of(1990, 1, 1), "123 Main St", List.of()
        );

        inactiveTraineeView = new TraineeWithListView(
                "John.Doe", "John", "Doe", false,
                LocalDate.of(1990, 1, 1), "123 Main St", List.of()
        );

        trainerInfo = new TrainerInfo("Jane.Smith", "Jane", "Smith", "Fitness");

        trainingView = new TrainingView(
                "Morning Session", LocalDate.now(), 60,
                "Fitness", "John.Doe", "Jane.Smith"
        );
    }

    // =========================================================================
    // REGISTER TRAINEE TESTS (100s)
    // =========================================================================

    @Test
    @Order(101)
    void registerTrainee_withValidRequest_shouldReturnCreated() {
        when(traineeService.createTrainee(createRequest)).thenReturn(createResponse);

        ResponseEntity<UserCreateResponse> response = traineeController.registerTrainee(createRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().username()).isEqualTo("John.Doe");
        assertThat(response.getBody().password()).isEqualTo("password123");
        verify(traineeService).createTrainee(createRequest);
    }

    @Test
    @Order(102)
    void registerTrainee_withMinimalFields_shouldReturnCreated() {
        TraineeCreateRequest minimalRequest = new TraineeCreateRequest(
                "John", "Doe", null, null
        );
        when(traineeService.createTrainee(minimalRequest)).thenReturn(createResponse);

        ResponseEntity<UserCreateResponse> response = traineeController.registerTrainee(minimalRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
    }

    // =========================================================================
    // GET TRAINEE PROFILE TESTS (200s)
    // =========================================================================

    @Test
    @Order(201)
    void getTraineeProfile_withValidUsername_shouldReturnProfile() {
        when(traineeService.getTrainee("John.Doe")).thenReturn(activeTraineeView);

        ResponseEntity<TraineeWithListView> response = traineeController.getTraineeProfile(
                "John.Doe"
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().username()).isEqualTo("John.Doe");
        assertThat(response.getBody().firstName()).isEqualTo("John");
        assertThat(response.getBody().lastName()).isEqualTo("Doe");
        assertThat(response.getBody().active()).isTrue();
    }

    @Test
    @Order(202)
    void getTraineeProfile_withTrainers_shouldReturnProfileWithTrainers() {
        TraineeWithListView viewWithTrainers = new TraineeWithListView(
                "John.Doe", "John", "Doe", true,
                LocalDate.of(1990, 1, 1), "123 Main St", List.of(trainerInfo)
        );
        when(traineeService.getTrainee("John.Doe")).thenReturn(viewWithTrainers);

        ResponseEntity<TraineeWithListView> response = traineeController.getTraineeProfile(
                "John.Doe"
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().trainers()).hasSize(1);
        assertThat(response.getBody().trainers().get(0).username()).isEqualTo("Jane.Smith");
    }

    @Test
    @Order(203)
    void getTraineeProfile_shouldCallServiceWithCorrectUsername() {
        when(traineeService.getTrainee("John.Doe")).thenReturn(activeTraineeView);

        traineeController.getTraineeProfile("John.Doe");

        verify(traineeService).getTrainee("John.Doe");
    }

    // =========================================================================
    // UPDATE TRAINEE PROFILE TESTS (300s)
    // =========================================================================

    @Test
    @Order(301)
    void updateTraineeProfile_withValidRequest_shouldReturnUpdatedProfile() {
        when(traineeService.updateTrainee(updateRequest)).thenReturn(activeTraineeView);

        ResponseEntity<TraineeWithListView> response = traineeController.updateTraineeProfile(
                "John.Doe", updateRequest
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        verify(traineeService).updateTrainee(updateRequest);
    }

    @Test
    @Order(302)
    void updateTraineeProfile_withPartialUpdate_shouldReturnUpdatedProfile() {
        TraineeUpdateRequest partialRequest = new TraineeUpdateRequest(
                "John.Doe", "Johnny", null, null, null, null
        );
        when(traineeService.updateTrainee(partialRequest)).thenReturn(activeTraineeView);

        ResponseEntity<TraineeWithListView> response = traineeController.updateTraineeProfile(
                "John.Doe", partialRequest
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(traineeService).updateTrainee(partialRequest);
    }

    // =========================================================================
    // DELETE TRAINEE TESTS (400s)
    // =========================================================================

    @Test
    @Order(401)
    void deleteTrainee_withValidUsername_shouldReturnOk() {
        when(traineeService.deleteTrainee("John.Doe")).thenReturn(true);

        ResponseEntity<Void> response = traineeController.deleteTrainee("John.Doe");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(traineeService).deleteTrainee("John.Doe");
    }

    @Test
    @Order(402)
    void deleteTrainee_shouldCallServiceWithCorrectUsername() {
        when(traineeService.deleteTrainee("Jane.Doe")).thenReturn(true);

        traineeController.deleteTrainee("Jane.Doe");

        verify(traineeService).deleteTrainee("Jane.Doe");
    }

    // =========================================================================
    // CHANGE PASSWORD TESTS (500s)
    // =========================================================================

    @Test
    @Order(501)
    void changePassword_withValidRequest_shouldReturnOk() {
        PasswordChangeRequest passwordRequest = new PasswordChangeRequest(
                "John.Doe", "oldPassword", "newPassword"
        );
        when(traineeService.changePassword(passwordRequest)).thenReturn(true);

        ResponseEntity<Void> response = traineeController.changePassword(
                "John.Doe", passwordRequest
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(traineeService).changePassword(passwordRequest);
    }

    @Test
    @Order(502)
    void changePassword_shouldCallServiceWithCorrectRequest() {
        PasswordChangeRequest passwordRequest = new PasswordChangeRequest(
                "John.Doe", "currentPass", "newSecurePass123"
        );
        when(traineeService.changePassword(passwordRequest)).thenReturn(true);

        traineeController.changePassword("John.Doe", passwordRequest);

        verify(traineeService).changePassword(argThat(req ->
                req.username().equals("John.Doe") &&
                        req.newPassword().equals("newSecurePass123")
        ));
    }

    // =========================================================================
    // ACTIVATE/DEACTIVATE TESTS (600s)
    // =========================================================================

    @Test
    @Order(601)
    void updateActivationStatus_activateInactiveUser_shouldReturnOk() {
        ActivationRequest activateRequest = new ActivationRequest("John.Doe", true);

        when(traineeService.getTrainee("John.Doe")).thenReturn(inactiveTraineeView);
        when(traineeService.activate(activateRequest)).thenReturn(new ActivationResponse(true));

        ResponseEntity<Void> response = traineeController.updateActivationStatus(
                "John.Doe", activateRequest
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(traineeService).activate(activateRequest);
        verify(traineeService, never()).deactivate(any());
    }

    @Test
    @Order(602)
    void updateActivationStatus_deactivateActiveUser_shouldReturnOk() {
        ActivationRequest deactivateRequest = new ActivationRequest("John.Doe", false);

        when(traineeService.getTrainee("John.Doe")).thenReturn(activeTraineeView);
        when(traineeService.deactivate(deactivateRequest)).thenReturn(new ActivationResponse(false));

        ResponseEntity<Void> response = traineeController.updateActivationStatus(
                "John.Doe", deactivateRequest
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(traineeService).deactivate(deactivateRequest);
        verify(traineeService, never()).activate(any());
    }

    @Test
    @Order(603)
    void updateActivationStatus_activateAlreadyActiveUser_shouldThrowUserAlreadyActiveException() {
        ActivationRequest activateRequest = new ActivationRequest("John.Doe", true);

        when(traineeService.getTrainee("John.Doe")).thenReturn(activeTraineeView);

        assertThatThrownBy(() -> traineeController.updateActivationStatus(
                "John.Doe", activateRequest
        )).isInstanceOf(UserAlreadyActiveException.class);

        verify(traineeService, never()).activate(any());
    }

    @Test
    @Order(604)
    void updateActivationStatus_deactivateAlreadyInactiveUser_shouldThrowUserAlreadyInactiveException() {
        ActivationRequest deactivateRequest = new ActivationRequest("John.Doe", false);

        when(traineeService.getTrainee("John.Doe")).thenReturn(inactiveTraineeView);

        assertThatThrownBy(() -> traineeController.updateActivationStatus(
                "John.Doe", deactivateRequest
        )).isInstanceOf(UserAlreadyInactiveException.class);

        verify(traineeService, never()).deactivate(any());
    }

    @Test
    @Order(605)
    void updateActivationStatus_shouldCheckCurrentStatusBeforeAction() {
        ActivationRequest activateRequest = new ActivationRequest("John.Doe", true);

        when(traineeService.getTrainee("John.Doe")).thenReturn(inactiveTraineeView);
        when(traineeService.activate(activateRequest)).thenReturn(new ActivationResponse(true));

        traineeController.updateActivationStatus("John.Doe", activateRequest);

        verify(traineeService).getTrainee("John.Doe");
    }

    // =========================================================================
    // GET UNASSIGNED TRAINERS TESTS (700s)
    // =========================================================================

    @Test
    @Order(701)
    void getUnassignedTrainers_withValidUsername_shouldReturnList() {
        when(traineeService.getUnassignedTrainers("John.Doe")).thenReturn(List.of(trainerInfo));

        ResponseEntity<List<TrainerInfo>> response = traineeController.getUnassignedTrainers(
                "John.Doe"
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getFirst().username()).isEqualTo("Jane.Smith");
    }

    @Test
    @Order(702)
    void getUnassignedTrainers_withNoAvailableTrainers_shouldReturnEmptyList() {
        when(traineeService.getUnassignedTrainers("John.Doe")).thenReturn(List.of());

        ResponseEntity<List<TrainerInfo>> response = traineeController.getUnassignedTrainers(
                "John.Doe"
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @Order(703)
    void getUnassignedTrainers_withMultipleTrainers_shouldReturnAllTrainers() {
        TrainerInfo trainer2 = new TrainerInfo("Tom.Wilson", "Tom", "Wilson", "Yoga");
        TrainerInfo trainer3 = new TrainerInfo("Bob.Brown", "Bob", "Brown", "Resistance");

        when(traineeService.getUnassignedTrainers("John.Doe"))
                .thenReturn(List.of(trainerInfo, trainer2, trainer3));

        ResponseEntity<List<TrainerInfo>> response = traineeController.getUnassignedTrainers(
                "John.Doe"
        );

        assertThat(response.getBody()).hasSize(3);
    }

    // =========================================================================
    // UPDATE TRAINERS TESTS (800s)
    // =========================================================================

    @Test
    @Order(801)
    void updateTrainers_withValidRequest_shouldReturnUpdatedList() {
        TraineeTrainerListUpdateRequest trainerListRequest = new TraineeTrainerListUpdateRequest(
                "John.Doe", List.of("Jane.Smith", "Tom.Wilson")
        );
        TrainerInfo trainer2 = new TrainerInfo("Tom.Wilson", "Tom", "Wilson", "Yoga");

        when(traineeService.updateTrainers(trainerListRequest))
                .thenReturn(List.of(trainerInfo, trainer2));

        ResponseEntity<List<TrainerInfo>> response = traineeController.updateTrainers(
                "John.Doe", trainerListRequest
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(2);
        verify(traineeService).updateTrainers(trainerListRequest);
    }

    @Test
    @Order(802)
    void updateTrainers_withEmptyList_shouldReturnEmptyList() {
        TraineeTrainerListUpdateRequest emptyRequest = new TraineeTrainerListUpdateRequest(
                "John.Doe", List.of()
        );
        when(traineeService.updateTrainers(emptyRequest)).thenReturn(List.of());

        ResponseEntity<List<TrainerInfo>> response = traineeController.updateTrainers(
                "John.Doe", emptyRequest
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @Order(803)
    void updateTrainers_withSingleTrainer_shouldReturnSingleTrainer() {
        TraineeTrainerListUpdateRequest singleRequest = new TraineeTrainerListUpdateRequest(
                "John.Doe", List.of("Jane.Smith")
        );
        when(traineeService.updateTrainers(singleRequest)).thenReturn(List.of(trainerInfo));

        ResponseEntity<List<TrainerInfo>> response = traineeController.updateTrainers(
                "John.Doe", singleRequest
        );

        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).username()).isEqualTo("Jane.Smith");
    }

    // =========================================================================
    // GET TRAININGS TESTS (900s)
    // =========================================================================

    @Test
    @Order(901)
    void getTrainings_withNoFilters_shouldReturnAllTrainings() {
        when(traineeService.getTrainings(any(TrainingSearchRequestForTrainee.class)))
                .thenReturn(List.of(trainingView));

        ResponseEntity<List<TrainingView>> response = traineeController.getTrainings(
                "John.Doe", null, null, null, null
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getFirst().trainingName()).isEqualTo("Morning Session");
    }

    @Test
    @Order(902)
    void getTrainings_withDateFilters_shouldPassFiltersToService() {
        LocalDate fromDate = LocalDate.of(2024, 1, 1);
        LocalDate toDate = LocalDate.of(2024, 12, 31);

        when(traineeService.getTrainings(any(TrainingSearchRequestForTrainee.class)))
                .thenReturn(List.of(trainingView));

        ResponseEntity<List<TrainingView>> response = traineeController.getTrainings(
                "John.Doe", fromDate, toDate, null, null
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(traineeService).getTrainings(argThat(req ->
                req.traineeUsername().equals("John.Doe") &&
                        req.fromDate().equals(fromDate) &&
                        req.toDate().equals(toDate)
        ));
    }

    @Test
    @Order(903)
    void getTrainings_withAllFilters_shouldPassAllFiltersToService() {
        LocalDate fromDate = LocalDate.of(2024, 1, 1);
        LocalDate toDate = LocalDate.of(2024, 12, 31);
        String trainerName = "Jane.Smith";
        String trainingType = "Fitness";

        when(traineeService.getTrainings(any(TrainingSearchRequestForTrainee.class)))
                .thenReturn(List.of(trainingView));

        ResponseEntity<List<TrainingView>> response = traineeController.getTrainings(
                "John.Doe", fromDate, toDate, trainerName, trainingType
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(traineeService).getTrainings(argThat(req ->
                req.traineeUsername().equals("John.Doe") &&
                        req.fromDate().equals(fromDate) &&
                        req.toDate().equals(toDate) &&
                        req.trainerUsername().equals(trainerName) &&
                        req.trainingType().equals(trainingType)
        ));
    }

    @Test
    @Order(904)
    void getTrainings_withNoResults_shouldReturnEmptyList() {
        when(traineeService.getTrainings(any(TrainingSearchRequestForTrainee.class)))
                .thenReturn(List.of());

        ResponseEntity<List<TrainingView>> response = traineeController.getTrainings(
                "John.Doe", null, null, null, null
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @Order(905)
    void getTrainings_withOnlyTrainerFilter_shouldPassCorrectRequest() {
        when(traineeService.getTrainings(any(TrainingSearchRequestForTrainee.class)))
                .thenReturn(List.of(trainingView));

        traineeController.getTrainings(
                "John.Doe", null, null, "Jane.Smith", null
        );

        verify(traineeService).getTrainings(argThat(req ->
                req.traineeUsername().equals("John.Doe") &&
                        req.fromDate() == null &&
                        req.toDate() == null &&
                        req.trainerUsername().equals("Jane.Smith") &&
                        req.trainingType() == null
        ));
    }

    @Test
    @Order(906)
    void getTrainings_withOnlyTrainingTypeFilter_shouldPassCorrectRequest() {
        when(traineeService.getTrainings(any(TrainingSearchRequestForTrainee.class)))
                .thenReturn(List.of(trainingView));

        traineeController.getTrainings(
                "John.Doe", null, null, null, "Yoga"
        );

        verify(traineeService).getTrainings(argThat(req ->
                req.traineeUsername().equals("John.Doe") &&
                        req.trainingType().equals("Yoga")
        ));
    }

    @Test
    @Order(907)
    void getTrainings_withMultipleTrainings_shouldReturnAllTrainings() {
        TrainingView training2 = new TrainingView(
                "Evening Session", LocalDate.now().plusDays(1), 45,
                "Yoga", "John.Doe", "Tom.Wilson"
        );

        when(traineeService.getTrainings(any(TrainingSearchRequestForTrainee.class)))
                .thenReturn(List.of(trainingView, training2));

        ResponseEntity<List<TrainingView>> response = traineeController.getTrainings(
                "John.Doe", null, null, null, null
        );

        assertThat(response.getBody()).hasSize(2);
    }
}
