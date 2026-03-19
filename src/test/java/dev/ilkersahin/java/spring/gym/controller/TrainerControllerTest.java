package dev.ilkersahin.java.spring.gym.controller;

import dev.ilkersahin.java.spring.gym.dto.request.*;
import dev.ilkersahin.java.spring.gym.dto.response.ActivationResponse;
import dev.ilkersahin.java.spring.gym.dto.response.UserCreateResponse;
import dev.ilkersahin.java.spring.gym.dto.view.TrainerWithListView;
import dev.ilkersahin.java.spring.gym.dto.view.TrainingView;
import dev.ilkersahin.java.spring.gym.dto.view.UserInfo;
import dev.ilkersahin.java.spring.gym.exception.UserAlreadyActiveException;
import dev.ilkersahin.java.spring.gym.exception.UserAlreadyInactiveException;
import dev.ilkersahin.java.spring.gym.service.TrainerService;
import org.junit.jupiter.api.*;
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TrainerControllerTest {

    @Mock private TrainerService trainerService;

    @Spy
    @InjectMocks
    private TrainerController trainerController;

    private TrainerCreateRequest createRequest;
    private TrainerUpdateRequest updateRequest;
    private UserCreateResponse createResponse;
    private TrainerWithListView activeTrainerView;
    private TrainerWithListView inactiveTrainerView;
    private UserInfo traineeInfo;
    private TrainingView trainingView;

    @BeforeEach
    void setUp() {
        createRequest = new TrainerCreateRequest(
                "Jane", "Smith", "Fitness"
        );

        updateRequest = new TrainerUpdateRequest(
                "Jane.Smith", "Jane", "Doe", true, "Yoga"
        );

        createResponse = new UserCreateResponse("Jane.Smith", "password123");

        activeTrainerView = new TrainerWithListView(
                "Jane.Smith", "Jane", "Smith", true, "Fitness", List.of()
        );

        inactiveTrainerView = new TrainerWithListView(
                "Jane.Smith", "Jane", "Smith", false, "Fitness", List.of()
        );

        traineeInfo = new UserInfo("John.Doe", "John", "Doe");

        trainingView = new TrainingView(
                "Morning Session", LocalDate.now(), 60,
                "Fitness", "John.Doe", "Jane.Smith"
        );
    }


    // =========================================================================
    // REGISTER TRAINER TESTS (100s)
    // =========================================================================

    @Test
    @Order(101)
    void registerTrainer_withValidRequest_shouldReturnCreated() {
        when(trainerService.createTrainer(createRequest)).thenReturn(createResponse);

        ResponseEntity<UserCreateResponse> response = trainerController.registerTrainer(createRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().username()).isEqualTo("Jane.Smith");
        assertThat(response.getBody().password()).isEqualTo("password123");
        verify(trainerService).createTrainer(createRequest);
    }

    @Test
    @Order(102)
    void registerTrainer_withDifferentSpecialization_shouldReturnCreated() {
        TrainerCreateRequest yogaRequest = new TrainerCreateRequest(
                "Tom", "Wilson", "Yoga"
        );
        UserCreateResponse yogaResponse = new UserCreateResponse("Tom.Wilson", "pass456");
        when(trainerService.createTrainer(yogaRequest)).thenReturn(yogaResponse);

        ResponseEntity<UserCreateResponse> response = trainerController.registerTrainer(yogaRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody().username()).isEqualTo("Tom.Wilson");
    }

    @Test
    @Order(103)
    void registerTrainer_shouldCallServiceWithCorrectRequest() {
        when(trainerService.createTrainer(any(TrainerCreateRequest.class))).thenReturn(createResponse);

        trainerController.registerTrainer(createRequest);

        verify(trainerService).createTrainer(argThat(req ->
                req.firstName().equals("Jane") &&
                        req.lastName().equals("Smith") &&
                        req.specialization().equals("Fitness")
        ));
    }

    // =========================================================================
    // GET TRAINER PROFILE TESTS (200s)
    // =========================================================================

    @Test
    @Order(201)
    void getTrainerProfile_withValidUsername_shouldReturnProfile() {
        when(trainerService.getTrainer("Jane.Smith")).thenReturn(activeTrainerView);

        ResponseEntity<TrainerWithListView> response = trainerController.getTrainerProfile(
                "Jane.Smith"
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().username()).isEqualTo("Jane.Smith");
        assertThat(response.getBody().firstName()).isEqualTo("Jane");
        assertThat(response.getBody().lastName()).isEqualTo("Smith");
        assertThat(response.getBody().active()).isTrue();
        assertThat(response.getBody().specialization()).isEqualTo("Fitness");
    }

    @Test
    @Order(202)
    void getTrainerProfile_withTrainees_shouldReturnProfileWithTrainees() {
        TrainerWithListView viewWithTrainees = new TrainerWithListView(
                "Jane.Smith", "Jane", "Smith", true,
                "Fitness", List.of(traineeInfo)
        );
        when(trainerService.getTrainer("Jane.Smith")).thenReturn(viewWithTrainees);

        ResponseEntity<TrainerWithListView> response = trainerController.getTrainerProfile(
                "Jane.Smith"
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().trainees()).hasSize(1);
        assertThat(response.getBody().trainees().get(0).username()).isEqualTo("John.Doe");
    }

    @Test
    @Order(203)
    void getTrainerProfile_withMultipleTrainees_shouldReturnAllTrainees() {
        UserInfo trainee2 = new UserInfo("Bob.Brown", "Bob", "Brown");
        UserInfo trainee3 = new UserInfo("Alice.White", "Alice", "White");
        TrainerWithListView viewWithTrainees = new TrainerWithListView(
                "Jane.Smith", "Jane", "Smith", true,
                "Fitness", List.of(traineeInfo, trainee2, trainee3)
        );
        when(trainerService.getTrainer("Jane.Smith")).thenReturn(viewWithTrainees);

        ResponseEntity<TrainerWithListView> response = trainerController.getTrainerProfile(
                "Jane.Smith"
        );

        assertThat(response.getBody().trainees()).hasSize(3);
    }

    @Test
    @Order(204)
    void getTrainerProfile_shouldCallServiceWithCorrectUsername() {
        when(trainerService.getTrainer("Jane.Smith")).thenReturn(activeTrainerView);

        trainerController.getTrainerProfile("Jane.Smith");

        verify(trainerService).getTrainer("Jane.Smith");
    }

    // =========================================================================
    // UPDATE TRAINER PROFILE TESTS (300s)
    // =========================================================================

    @Test
    @Order(301)
    void updateTrainerProfile_withValidRequest_shouldReturnUpdatedProfile() {
        when(trainerService.updateTrainer(updateRequest)).thenReturn(activeTrainerView);

        ResponseEntity<TrainerWithListView> response = trainerController.updateTrainerProfile(
                "Jane.Smith", updateRequest
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        verify(trainerService).updateTrainer(updateRequest);
    }

    @Test
    @Order(302)
    void updateTrainerProfile_withNewSpecialization_shouldReturnUpdatedProfile() {
        TrainerUpdateRequest specializationUpdate = new TrainerUpdateRequest(
                "Jane.Smith", "Jane", "Smith", true, "Yoga"
        );
        TrainerWithListView updatedView = new TrainerWithListView(
                "Jane.Smith", "Jane", "Smith", true,
                "Yoga", List.of()
        );
        when(trainerService.updateTrainer(specializationUpdate)).thenReturn(updatedView);

        ResponseEntity<TrainerWithListView> response = trainerController.updateTrainerProfile(
                "Jane.Smith", specializationUpdate
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(trainerService).updateTrainer(specializationUpdate);
    }

    @Test
    @Order(303)
    void updateTrainerProfile_withNameChange_shouldReturnUpdatedProfile() {
        TrainerUpdateRequest nameUpdate = new TrainerUpdateRequest(
                "Jane.Smith", "Janet", "Johnson", true, "Fitness"
        );
        when(trainerService.updateTrainer(nameUpdate)).thenReturn(activeTrainerView);

        ResponseEntity<TrainerWithListView> response = trainerController.updateTrainerProfile(
                "Jane.Smith", nameUpdate
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(trainerService).updateTrainer(argThat(req ->
                req.firstName().equals("Janet") &&
                        req.lastName().equals("Johnson")
        ));
    }

    // =========================================================================
    // CHANGE PASSWORD TESTS (400s)
    // =========================================================================

    @Test
    @Order(401)
    void changePassword_withValidRequest_shouldReturnOk() {
        PasswordChangeRequest passwordRequest = new PasswordChangeRequest(
                "Jane.Smith", "oldPassword", "newPassword"
        );
        when(trainerService.changePassword(passwordRequest)).thenReturn(true);

        ResponseEntity<Void> response = trainerController.changePassword(
                "Jane.Smith", passwordRequest
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(trainerService).changePassword(passwordRequest);
    }

    @Test
    @Order(402)
    void changePassword_shouldCallServiceWithCorrectRequest() {
        PasswordChangeRequest passwordRequest = new PasswordChangeRequest(
                "Jane.Smith", "currentPass", "newSecurePass123"
        );
        when(trainerService.changePassword(passwordRequest)).thenReturn(true);

        trainerController.changePassword("Jane.Smith", passwordRequest);

        verify(trainerService).changePassword(argThat(req ->
                req.username().equals("Jane.Smith") &&
                        req.oldPassword().equals("currentPass") &&
                        req.newPassword().equals("newSecurePass123")
        ));
    }

    // =========================================================================
    // ACTIVATE/DEACTIVATE TESTS (500s)
    // =========================================================================

    @Test
    @Order(501)
    void updateActivationStatus_activateInactiveUser_shouldReturnOk() {
        ActivationRequest activateRequest = new ActivationRequest("Jane.Smith", true);

        when(trainerService.getTrainer("Jane.Smith")).thenReturn(inactiveTrainerView);
        when(trainerService.activate(activateRequest)).thenReturn(new ActivationResponse(true));

        ResponseEntity<Void> response = trainerController.updateActivationStatus(
                "Jane.Smith", activateRequest
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(trainerService).activate(activateRequest);
        verify(trainerService, never()).deactivate(any());
    }

    @Test
    @Order(502)
    void updateActivationStatus_deactivateActiveUser_shouldReturnOk() {
        ActivationRequest deactivateRequest = new ActivationRequest("Jane.Smith", false);

        when(trainerService.getTrainer("Jane.Smith")).thenReturn(activeTrainerView);
        when(trainerService.deactivate(deactivateRequest)).thenReturn(new ActivationResponse(false));

        ResponseEntity<Void> response = trainerController.updateActivationStatus(
                "Jane.Smith", deactivateRequest
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(trainerService).deactivate(deactivateRequest);
        verify(trainerService, never()).activate(any());
    }

    @Test
    @Order(503)
    void updateActivationStatus_activateAlreadyActiveUser_shouldThrowUserAlreadyActiveException() {
        ActivationRequest activateRequest = new ActivationRequest("Jane.Smith", true);

        when(trainerService.getTrainer("Jane.Smith")).thenReturn(activeTrainerView);

        assertThatThrownBy(() -> trainerController.updateActivationStatus(
                "Jane.Smith", activateRequest
        )).isInstanceOf(UserAlreadyActiveException.class);

        verify(trainerService, never()).activate(any());
        verify(trainerService, never()).deactivate(any());
    }

    @Test
    @Order(504)
    void updateActivationStatus_deactivateAlreadyInactiveUser_shouldThrowUserAlreadyInactiveException() {
        ActivationRequest deactivateRequest = new ActivationRequest("Jane.Smith", false);

        when(trainerService.getTrainer("Jane.Smith")).thenReturn(inactiveTrainerView);

        assertThatThrownBy(() -> trainerController.updateActivationStatus(
                "Jane.Smith", deactivateRequest
        )).isInstanceOf(UserAlreadyInactiveException.class);

        verify(trainerService, never()).activate(any());
        verify(trainerService, never()).deactivate(any());
    }

    @Test
    @Order(505)
    void updateActivationStatus_shouldCheckCurrentStatusBeforeAction() {
        ActivationRequest activateRequest = new ActivationRequest("Jane.Smith", true);

        when(trainerService.getTrainer("Jane.Smith")).thenReturn(inactiveTrainerView);
        when(trainerService.activate(activateRequest)).thenReturn(new ActivationResponse(true));

        trainerController.updateActivationStatus("Jane.Smith", activateRequest);

        verify(trainerService).getTrainer("Jane.Smith");
    }

    // =========================================================================
    // GET TRAININGS TESTS (600s)
    // =========================================================================

    @Test
    @Order(601)
    void getTrainings_withNoFilters_shouldReturnAllTrainings() {
        when(trainerService.getTrainings(any(TrainingSearchRequestForTrainer.class)))
                .thenReturn(List.of(trainingView));

        ResponseEntity<List<TrainingView>> response = trainerController.getTrainings(
                "Jane.Smith", null, null, null
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).trainingName()).isEqualTo("Morning Session");
        assertThat(response.getBody().get(0).trainerUsername()).isEqualTo("Jane.Smith");
    }

    @Test
    @Order(602)
    void getTrainings_withDateFilters_shouldPassFiltersToService() {
        LocalDate fromDate = LocalDate.of(2024, 1, 1);
        LocalDate toDate = LocalDate.of(2024, 12, 31);

        when(trainerService.getTrainings(any(TrainingSearchRequestForTrainer.class)))
                .thenReturn(List.of(trainingView));

        ResponseEntity<List<TrainingView>> response = trainerController.getTrainings(
                "Jane.Smith", fromDate, toDate, null
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(trainerService).getTrainings(argThat(req ->
                req.trainerUsername().equals("Jane.Smith") &&
                        req.fromDate().equals(fromDate) &&
                        req.toDate().equals(toDate) &&
                        req.traineeUsername() == null
        ));
    }

    @Test
    @Order(603)
    void getTrainings_withAllFilters_shouldPassAllFiltersToService() {
        LocalDate fromDate = LocalDate.of(2024, 1, 1);
        LocalDate toDate = LocalDate.of(2024, 12, 31);
        String traineeName = "John.Doe";

        when(trainerService.getTrainings(any(TrainingSearchRequestForTrainer.class)))
                .thenReturn(List.of(trainingView));

        ResponseEntity<List<TrainingView>> response = trainerController.getTrainings(
                "Jane.Smith", fromDate, toDate, traineeName
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(trainerService).getTrainings(argThat(req ->
                req.trainerUsername().equals("Jane.Smith") &&
                        req.fromDate().equals(fromDate) &&
                        req.toDate().equals(toDate) &&
                        req.traineeUsername().equals(traineeName)
        ));
    }

    @Test
    @Order(604)
    void getTrainings_withNoResults_shouldReturnEmptyList() {
        when(trainerService.getTrainings(any(TrainingSearchRequestForTrainer.class)))
                .thenReturn(List.of());

        ResponseEntity<List<TrainingView>> response = trainerController.getTrainings(
                "Jane.Smith", null, null, null
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @Order(605)
    void getTrainings_withOnlyTraineeFilter_shouldPassCorrectRequest() {
        when(trainerService.getTrainings(any(TrainingSearchRequestForTrainer.class)))
                .thenReturn(List.of(trainingView));

        trainerController.getTrainings(
                "Jane.Smith", null, null, "John.Doe"
        );

        verify(trainerService).getTrainings(argThat(req ->
                req.trainerUsername().equals("Jane.Smith") &&
                        req.fromDate() == null &&
                        req.toDate() == null &&
                        req.traineeUsername().equals("John.Doe")
        ));
    }

    @Test
    @Order(606)
    void getTrainings_withOnlyFromDate_shouldPassCorrectRequest() {
        LocalDate fromDate = LocalDate.of(2024, 6, 1);

        when(trainerService.getTrainings(any(TrainingSearchRequestForTrainer.class)))
                .thenReturn(List.of(trainingView));

        trainerController.getTrainings(
                "Jane.Smith", fromDate, null, null
        );

        verify(trainerService).getTrainings(argThat(req ->
                req.trainerUsername().equals("Jane.Smith") &&
                        req.fromDate().equals(fromDate) &&
                        req.toDate() == null
        ));
    }

    @Test
    @Order(607)
    void getTrainings_withOnlyToDate_shouldPassCorrectRequest() {
        LocalDate toDate = LocalDate.of(2024, 12, 31);

        when(trainerService.getTrainings(any(TrainingSearchRequestForTrainer.class)))
                .thenReturn(List.of(trainingView));

        trainerController.getTrainings(
                "Jane.Smith", null, toDate, null
        );

        verify(trainerService).getTrainings(argThat(req ->
                req.trainerUsername().equals("Jane.Smith") &&
                        req.fromDate() == null &&
                        req.toDate().equals(toDate)
        ));
    }

    @Test
    @Order(608)
    void getTrainings_withMultipleTrainings_shouldReturnAllTrainings() {
        TrainingView training2 = new TrainingView(
                "Evening Session", LocalDate.now().plusDays(1), 45,
                "Yoga", "Bob.Brown", "Jane.Smith"
        );
        TrainingView training3 = new TrainingView(
                "Weekend Workout", LocalDate.now().plusDays(2), 90,
                "Fitness", "Alice.White", "Jane.Smith"
        );

        when(trainerService.getTrainings(any(TrainingSearchRequestForTrainer.class)))
                .thenReturn(List.of(trainingView, training2, training3));

        ResponseEntity<List<TrainingView>> response = trainerController.getTrainings(
                "Jane.Smith", null, null, null
        );

        assertThat(response.getBody()).hasSize(3);
        assertThat(response.getBody().get(0).trainingName()).isEqualTo("Morning Session");
        assertThat(response.getBody().get(1).trainingName()).isEqualTo("Evening Session");
        assertThat(response.getBody().get(2).trainingName()).isEqualTo("Weekend Workout");
    }
}
