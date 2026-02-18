package dev.ilkersahin.java.spring.gym.service.impl;

import dev.ilkersahin.java.spring.gym.dto.request.ActivationRequest;
import dev.ilkersahin.java.spring.gym.dto.request.PasswordChangeRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TraineeCreateRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TraineeTrainerListUpdateRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TraineeUpdateRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TrainingSearchRequestForTrainee;
import dev.ilkersahin.java.spring.gym.dto.response.ActivationResponse;
import dev.ilkersahin.java.spring.gym.dto.response.UserCreateResponse;
import dev.ilkersahin.java.spring.gym.dto.view.TraineeView;
import dev.ilkersahin.java.spring.gym.dto.view.TraineeWithListView;
import dev.ilkersahin.java.spring.gym.dto.view.TrainerInfo;
import dev.ilkersahin.java.spring.gym.dto.view.TrainingView;
import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.model.Training;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import dev.ilkersahin.java.spring.gym.model.User;
import dev.ilkersahin.java.spring.gym.repository.TraineeRepository;
import dev.ilkersahin.java.spring.gym.repository.TrainerRepository;
import dev.ilkersahin.java.spring.gym.repository.TrainingRepository;
import dev.ilkersahin.java.spring.gym.repository.UserRepository;
import dev.ilkersahin.java.spring.gym.service.TraineeService;
import dev.ilkersahin.java.spring.gym.service.util.PasswordGeneratorService;
import dev.ilkersahin.java.spring.gym.service.util.UsernameGeneratorService;
import dev.ilkersahin.java.spring.gym.service.util.ViewMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TraineeServiceImpl implements TraineeService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final UserRepository userRepository;
    private final UsernameGeneratorService usernameGeneratorService;
    private final PasswordGeneratorService passwordGeneratorService;
    private final ViewMapper viewMapper;

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @Override
    public UserCreateResponse createTrainee(@Valid TraineeCreateRequest request) {
        log.info("Creating Trainee: {} {}", request.firstName(), request.lastName());

        String username = usernameGeneratorService.generateUniqueUsername(request.firstName(), request.lastName());
        String password = passwordGeneratorService.generate(10);

        User user = new User(request.firstName(), request.lastName(), username, password, true);
        userRepository.save(user);

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());

        Trainee saved = traineeRepository.save(trainee);

        log.info("Trainee created with username '{}'", saved.getUsername());

        return new UserCreateResponse(saved.getUsername(), saved.getPassword());
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public TraineeWithListView getTrainee(@NotBlank String username) {
        log.debug("Fetching trainee '{}'", username);
        Trainee trainee = findTraineeOrThrow(username);
        List<Trainer> trainers = traineeRepository.findAssignedTrainers(username);
        return toView(trainee, trainers);
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    @Override
    public TraineeWithListView updateTrainee(@Valid TraineeUpdateRequest request) {
        Trainee trainee = findTraineeOrThrow(request.username());

        log.info("Updating trainee '{}'", request.username());

        if (request.firstName() != null) {
            trainee.getUser().setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            trainee.getUser().setLastName(request.lastName());
        }
        if (request.active() != null) {
            trainee.getUser().setActive(request.active());
        }
        if (request.dateOfBirth() != null) {
            trainee.setDateOfBirth(request.dateOfBirth());
        }
        if (request.address() != null) {
            trainee.setAddress(request.address());
        }

        traineeRepository.save(trainee);

        List<Trainer> trainers = traineeRepository.findAssignedTrainers(request.username());
        return toView(trainee, trainers);
    }

    // -------------------------------------------------------------------------
    // PASSWORD
    // -------------------------------------------------------------------------

    @Override
    public Boolean changePassword(@Valid PasswordChangeRequest request) {
        Trainee trainee = findTraineeOrThrow(request.username());
        log.warn("Changing password of trainee '{}'", request.username());
        trainee.getUser().setPassword(request.newPassword());
        userRepository.save(trainee.getUser());
        return true;
    }

    // -------------------------------------------------------------------------
    // ACTIVATE / DEACTIVATE
    // -------------------------------------------------------------------------

    @Override
    public ActivationResponse activate(@Valid ActivationRequest request) {
        Trainee trainee = findTraineeOrThrow(request.username());
        log.info("Activating trainee '{}'", request.username());
        trainee.getUser().setActive(true);
        return new ActivationResponse(true);
    }

    @Override
    public ActivationResponse deactivate(@Valid ActivationRequest request) {
        Trainee trainee = findTraineeOrThrow(request.username());
        log.warn("Deactivating trainee '{}'", request.username());
        trainee.getUser().setActive(false);
        return new ActivationResponse(false);
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @Override
    public Boolean deleteTrainee(String username) {
        Trainee trainee = findTraineeOrThrow(username);
        log.warn("Deleting trainee '{}'", username);

        User user = trainee.getUser();
        user.setTrainee(null);
        traineeRepository.delete(trainee);
        userRepository.delete(user);
        return true;
    }

    // -------------------------------------------------------------------------
    // TRAINERS
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<TrainerInfo> getUnassignedTrainers(@NotBlank String username) {
        Trainee trainee = findTraineeOrThrow(username);

        log.info("Getting unassigned trainers for trainee '{}'", username);

        List<Trainer> trainers = trainerRepository.findTrainersNotAssignedToTrainee(trainee.getUser().getUsername());

        return toTrainerInfoList(trainers);
    }

    @Override
    public List<TrainerInfo> updateTrainers(@Valid TraineeTrainerListUpdateRequest request) {
        Trainee trainee = findTraineeOrThrow(request.traineeUsername());

        log.warn("Updating trainers for trainee '{}'", request.traineeUsername());

        List<Trainer> trainers = trainerRepository.findByUserUsernames(request.trainerUsernames());

        trainee.getTrainers().clear();
        trainee.getTrainers().addAll(trainers);
        traineeRepository.save(trainee);

        log.info("Updated trainers for trainee {}", trainee.getUser().getUsername());

        return toTrainerInfoList(trainers);
    }

    // -------------------------------------------------------------------------
    // TRAININGS
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<TrainingView> getTrainings(@Valid TrainingSearchRequestForTrainee request) {
        findTraineeOrThrow(request.traineeUsername());

        log.info("Getting trainings for trainee '{}'", request.traineeUsername());

        List<Training> trainings = trainingRepository.findForTrainee(
                request.traineeUsername(),
                request.fromDate(),
                request.toDate(),
                request.trainerUsername(),
                request.trainingType() == null ? null : TrainingType.Type.fromName(request.trainingType())
        );

        return trainings.stream()
                .map(viewMapper::toView)
                .toList();
    }


    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private Trainee findTraineeOrThrow(String username) {
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found"));
        return trainee;
    }

    private TraineeView toView(Trainee trainee) {
        return viewMapper.toView(trainee);
    }

    private TraineeWithListView toView(Trainee trainee, List<Trainer> trainers) {
        return new TraineeWithListView(
                trainee.getUsername(), trainee.getFirstName(), trainee.getLastName(),
                trainee.isActive(), trainee.getDateOfBirth(), trainee.getAddress(),
                toTrainerInfoList(trainers)
        );
    }

    private List<TrainerInfo> toTrainerInfoList(List<Trainer> trainers) {
        return trainers.stream().map(this::toTrainerInfo).toList();
    }

    private TrainerInfo toTrainerInfo(Trainer trainer) {
        return new TrainerInfo(
                trainer.getUsername(), trainer.getFirstName(), trainer.getLastName(),
                trainer.getSpecializationType().getName()
        );
    }
}
