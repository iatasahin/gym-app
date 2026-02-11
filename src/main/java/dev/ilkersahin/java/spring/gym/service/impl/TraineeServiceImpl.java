package dev.ilkersahin.java.spring.gym.service.impl;

import dev.ilkersahin.java.spring.gym.dao.TraineeDao;
import dev.ilkersahin.java.spring.gym.dao.TrainerDao;
import dev.ilkersahin.java.spring.gym.dao.TrainingDao;
import dev.ilkersahin.java.spring.gym.dao.UserDao;
import dev.ilkersahin.java.spring.gym.dto.request.*;
import dev.ilkersahin.java.spring.gym.dto.response.*;
import dev.ilkersahin.java.spring.gym.dto.view.*;
import dev.ilkersahin.java.spring.gym.model.*;
import dev.ilkersahin.java.spring.gym.service.TraineeService;
import dev.ilkersahin.java.spring.gym.service.util.PasswordGeneratorService;
import dev.ilkersahin.java.spring.gym.service.util.UsernameGeneratorService;
import dev.ilkersahin.java.spring.gym.service.util.ViewMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TraineeServiceImpl implements TraineeService {
    private static final Logger log = LoggerFactory.getLogger(TraineeServiceImpl.class);

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDAO;
    private final TrainingDao trainingDao;
    private final UserDao userDao;
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
        userDao.persist(user);

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());

        Trainee saved = traineeDao.createTrainee(trainee);

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
        List<Trainer> trainers = traineeDao.findAssignedTrainers(username);
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

        traineeDao.updateTrainee(trainee);

        List<Trainer> trainers = traineeDao.findAssignedTrainers(request.username());
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
        userDao.merge(trainee.getUser());
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

        trainee.getUser().setTrainee(null);
        traineeDao.deleteTrainee(username);
        userDao.deleteUser(username);
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

        List<Trainer> trainers = trainerDAO.findTrainersNotAssignedToTrainee(trainee.getUser().getUsername());

        return toTrainerInfoList(trainers);
    }

    @Override
    public List<TrainerInfo> updateTrainers(@Valid TraineeTrainerListUpdateRequest request) {
        Trainee trainee = findTraineeOrThrow(request.traineeUsername());

        log.warn("Updating trainers for trainee '{}'", request.traineeUsername());

        List<Trainer> trainers = trainerDAO.findByUsernames(request.trainerUsernames());

        traineeDao.updateTrainers(trainee.getUser().getUsername(), trainers);

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

        List<Training> trainings = trainingDao.findForTrainee(
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
        Trainee trainee = traineeDao.getTrainee(username)
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
