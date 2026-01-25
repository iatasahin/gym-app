package dev.ilkersahin.java.spring.gym.service.impl;

import dev.ilkersahin.java.spring.gym.dao.TrainerDao;
import dev.ilkersahin.java.spring.gym.dao.TrainingDao;
import dev.ilkersahin.java.spring.gym.dao.UserDao;
import dev.ilkersahin.java.spring.gym.dto.auth.Credentials;
import dev.ilkersahin.java.spring.gym.dto.request.*;
import dev.ilkersahin.java.spring.gym.dto.response.*;
import dev.ilkersahin.java.spring.gym.dto.view.TrainerView;
import dev.ilkersahin.java.spring.gym.dto.view.TrainingView;
import dev.ilkersahin.java.spring.gym.model.*;
import dev.ilkersahin.java.spring.gym.service.TrainerService;
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
public class TrainerServiceImpl implements TrainerService {
    private static final Logger log = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private final TrainerDao trainerDao;
    private final TrainingDao trainingDao;
    private final UserDao userDao;
    private final UsernameGeneratorService usernameGeneratorService;
    private final PasswordGeneratorService passwordGeneratorService;
    private final ViewMapper viewMapper;

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @Override
    public TrainerCreateResponse createTrainer(@Valid TrainerCreateRequest request) {
        log.info("Creating trainer: {} {}", request.firstName(), request.lastName());

        String username = usernameGeneratorService.generateUniqueUsername(request.firstName(), request.lastName());
        String password = passwordGeneratorService.generate(10);

        User user = new User(request.firstName(), request.lastName(), username, password, true);
        userDao.persist(user);

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(TrainingType.fromEnum(request.specialization()));

        Trainer saved = trainerDao.createTrainer(trainer);

        log.info("Trainer created with username '{}'", saved.getUsername());

        return new TrainerCreateResponse(toView(saved), password);
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public TrainerGetResponse getTrainer(@Valid TrainerGetRequest request) {
        Trainer trainer = authenticate(request.credentials());
        log.debug("Fetching trainer '{}'", request.credentials().username());
        return new TrainerGetResponse(toView(trainer));
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    @Override
    public TrainerUpdateResponse updateTrainer(@Valid TrainerUpdateRequest request) {
        Trainer trainer = authenticate(request.credentials());

        log.info("Updating trainer '{}'", request.credentials().username());

        if (request.firstName() != null) {
            trainer.getUser().setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            trainer.getUser().setLastName(request.lastName());
        }
        if (request.specialization() != null) {
            trainer.setSpecialization(TrainingType.fromEnum(request.specialization()));
        }

        trainerDao.updateTrainer(trainer);
        return new TrainerUpdateResponse(true);
    }

    // -------------------------------------------------------------------------
    // PASSWORD
    // -------------------------------------------------------------------------

    @Override
    public TrainerPasswordChangeResponse changePassword(@Valid TrainerPasswordChangeRequest request) {
        Trainer trainer = authenticate(request.trainerCredentials());
        log.warn("Changing password of trainer '{}'", request.trainerCredentials().username());
        trainer.getUser().setPassword(request.newPassword());
        userDao.merge(trainer.getUser());
        return new TrainerPasswordChangeResponse(true);
    }

    // -------------------------------------------------------------------------
    // ACTIVATE / DEACTIVATE
    // -------------------------------------------------------------------------

    @Override
    public ActivationResponse activate(@Valid ActivationRequest request) {
        Trainer trainer = authenticate(request.credentials());
        log.info("Activating trainer '{}'", request.credentials().username());
        trainer.getUser().setActive(true);
        return new ActivationResponse(true);
    }

    @Override
    public ActivationResponse deactivate(@Valid ActivationRequest request) {
        Trainer trainer = authenticate(request.credentials());
        log.warn("Deactivating trainer '{}'", request.credentials().username());
        trainer.getUser().setActive(false);
        return new ActivationResponse(false);
    }

    // -------------------------------------------------------------------------
    // TRAININGS
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public TrainingSearchResponse getTrainings(@Valid TrainingSearchRequestForTrainer request) {
        authenticate(request.credentials());

        log.info("Getting trainings for trainer '{}'", request.credentials().username());

        List<Training> trainings =
                trainingDao.findForTrainer(
                        request.credentials().username(),
                        request.fromDate(),
                        request.toDate(),
                        request.traineeUsername()
                );

        List<TrainingView> views = trainings.stream()
                .map(viewMapper::toView)
                .toList();

        return new TrainingSearchResponse(views);
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private Trainer authenticate(Credentials credentials) {
        Trainer trainer = trainerDao.getTrainer(credentials.username())
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found"));

        if (!trainer.getUser().getPassword().equals(credentials.password())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        if (!trainer.getUser().isActive()) {
            throw new IllegalStateException("Trainer is inactive");
        }
        return trainer;
    }

    private TrainerView toView(Trainer trainer) {
        return viewMapper.toView(trainer);
    }
}
