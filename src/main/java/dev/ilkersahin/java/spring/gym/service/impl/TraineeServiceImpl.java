package dev.ilkersahin.java.spring.gym.service.impl;

import dev.ilkersahin.java.spring.gym.dao.TraineeDao;
import dev.ilkersahin.java.spring.gym.dao.TrainerDao;
import dev.ilkersahin.java.spring.gym.dao.TrainingDao;
import dev.ilkersahin.java.spring.gym.dao.UserDao;
import dev.ilkersahin.java.spring.gym.dto.auth.Credentials;
import dev.ilkersahin.java.spring.gym.dto.request.*;
import dev.ilkersahin.java.spring.gym.dto.response.*;
import dev.ilkersahin.java.spring.gym.dto.view.TraineeView;
import dev.ilkersahin.java.spring.gym.dto.view.TrainerView;
import dev.ilkersahin.java.spring.gym.dto.view.TrainingView;
import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.model.Training;
import dev.ilkersahin.java.spring.gym.model.User;
import dev.ilkersahin.java.spring.gym.service.TraineeService;
import dev.ilkersahin.java.spring.gym.service.util.PasswordGeneratorService;
import dev.ilkersahin.java.spring.gym.service.util.UsernameGeneratorService;
import dev.ilkersahin.java.spring.gym.service.util.ViewMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
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
    public TraineeCreateResponse createTrainee(@Valid TraineeCreateRequest request) {
        log.info("Creating Trainee: {} {}", request.firstName(), request.lastName());

        String username = usernameGeneratorService.generateUniqueUsername(request.firstName(), request.lastName());
        String password = passwordGeneratorService.generate(10);

        User user = new User(request.firstName(), request.lastName(), username, password, true);

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());

        Trainee saved = traineeDao.createTrainee(trainee);

        log.info("Trainee created with username '{}'", saved.getUsername());

        return new TraineeCreateResponse(toView(saved));
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public TraineeGetResponse getTrainee(@Valid TraineeGetRequest request) {
        Trainee trainee = authenticate(request.credentials());
        log.debug("Fetching trainee '{}'", request.credentials().username());
        return new TraineeGetResponse(toView(trainee));
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    @Override
    public TraineeUpdateResponse updateTrainee(@Valid TraineeUpdateRequest request) {
        Trainee trainee = authenticate(request.credentials());

        log.info("Updating trainee '{}'", request.credentials().username());

        if (request.firstName() != null) {
            trainee.getUser().setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            trainee.getUser().setLastName(request.lastName());
        }
        if (request.dateOfBirth() != null) {
            trainee.setDateOfBirth(request.dateOfBirth());
        }
        if (request.address() != null) {
            trainee.setAddress(request.address());
        }

        traineeDao.updateTrainee(trainee);
        return new TraineeUpdateResponse(true);
    }

    // -------------------------------------------------------------------------
    // PASSWORD
    // -------------------------------------------------------------------------

    @Override
    public TraineePasswordChangeResponse changePassword(@Valid TraineePasswordChangeRequest request) {
        Trainee trainee = authenticate(request.credentials());
        log.warn("Changing password of trainee '{}'", request.credentials().username());
        trainee.getUser().setPassword(request.newPassword());
        userDao.merge(trainee.getUser());
        return new TraineePasswordChangeResponse(true);
    }

    // -------------------------------------------------------------------------
    // ACTIVATE / DEACTIVATE
    // -------------------------------------------------------------------------

    @Override
    public ActivationResponse activate(@Valid ActivationRequest request) {
        Trainee trainee = authenticate(request.credentials());
        log.info("Activating trainee '{}'", request.credentials().username());
        trainee.getUser().setActive(true);
        return new ActivationResponse(true);
    }

    @Override
    public ActivationResponse deactivate(@Valid ActivationRequest request) {
        Trainee trainee = authenticate(request.credentials());
        log.warn("Deactivating trainee '{}'", request.credentials().username());
        trainee.getUser().setActive(false);
        return new ActivationResponse(false);
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @Override
    public TraineeDeleteResponse deleteTrainee(@Valid TraineeDeleteRequest request) {
        authenticate(request.credentials());
        log.warn("Deleting trainee '{}'", request.credentials().username());
        traineeDao.deleteTrainee(request.credentials().username());
        return new TraineeDeleteResponse(true);
    }

    // -------------------------------------------------------------------------
    // TRAINERS
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public TrainerListResponse getUnassignedTrainers(@Valid TraineeGetRequest request) {
        Trainee trainee = authenticate(request.credentials());

        log.info("Getting unassigned trainers for trainee '{}'", request.credentials().username());

        List<Trainer> trainers = trainerDAO.findTrainersNotAssignedToTrainee(trainee.getUser().getUsername());

        List<TrainerView> views = trainers.stream()
                .map(viewMapper::toView)
                .toList();

        return new TrainerListResponse(views);
    }

    @Override
    public TraineeUpdateResponse updateTrainers(@Valid TraineeTrainerUpdateRequest request) {
        Trainee trainee = authenticate(request.credentials());

        log.warn("Updating trainers for trainee '{}'", request.credentials().username());

        List<Trainer> trainers = trainerDAO.findByUsernames(request.trainerUsernames());

        traineeDao.updateTrainers(trainee.getUser().getUsername(), trainers);

        log.info("Updated trainers for trainee {}", trainee.getUser().getUsername());

        return new TraineeUpdateResponse(true);
    }

    // -------------------------------------------------------------------------
    // TRAININGS
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public TrainingSearchResponse getTrainings(@Valid TrainingSearchRequestForTrainee request) {
        authenticate(request.credentials());

        log.info("Getting trainings for trainee '{}'", request.credentials().username());

        List<Training> trainings = trainingDao.findForTrainee(
                request.credentials().username(),
                request.fromDate(),
                request.toDate(),
                request.trainerUsername(),
                request.trainingType()
        );

        List<TrainingView> views = trainings.stream()
                .map(viewMapper::toView)
                .toList();

        return new TrainingSearchResponse(views);
    }


    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private Trainee authenticate(Credentials credentials) {
        Trainee trainee = traineeDao.getTrainee(credentials.username())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found"));

        if (!trainee.getUser().getPassword().equals(credentials.password())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        if (!trainee.getUser().isActive()) {
            throw new IllegalStateException("Trainee is inactive");
        }
        return trainee;
    }

    private TraineeView toView(Trainee trainee) {
        return viewMapper.toView(trainee);
    }
}
