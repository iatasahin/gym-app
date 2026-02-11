package dev.ilkersahin.java.spring.gym.service.impl;

import dev.ilkersahin.java.spring.gym.dao.TrainerDao;
import dev.ilkersahin.java.spring.gym.dao.TrainingDao;
import dev.ilkersahin.java.spring.gym.dao.UserDao;
import dev.ilkersahin.java.spring.gym.dto.request.*;
import dev.ilkersahin.java.spring.gym.dto.response.*;
import dev.ilkersahin.java.spring.gym.dto.view.*;
import dev.ilkersahin.java.spring.gym.exception.InvalidCredentialsException;
import dev.ilkersahin.java.spring.gym.model.*;
import dev.ilkersahin.java.spring.gym.service.TrainerService;
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
    public UserCreateResponse createTrainer(@Valid TrainerCreateRequest request) {
        log.info("Creating trainer: {} {}", request.firstName(), request.lastName());

        String username = usernameGeneratorService.generateUniqueUsername(request.firstName(), request.lastName());
        String password = passwordGeneratorService.generate(10);

        User user = new User(request.firstName(), request.lastName(), username, password, true);
        userDao.persist(user);

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(TrainingType.fromEnum(TrainingType.Type.fromName(request.specialization())));

        Trainer saved = trainerDao.createTrainer(trainer);

        log.info("Trainer created with username '{}'", saved.getUsername());

        return new UserCreateResponse(saved.getUsername(), saved.getPassword());
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public TrainerWithListView getTrainer(String username) {
        log.debug("Fetching trainer '{}'", username);
        Trainer trainer = findTrainerOrThrow(username);
        List<Trainee> trainees = trainerDao.findAssignedTrainees(username);
        return toView(trainer, trainees);
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    @Override
    public TrainerWithListView updateTrainer(@Valid TrainerUpdateRequest request) {
        Trainer trainer = findTrainerOrThrow(request.username());

        log.info("Updating trainer '{}'", request.username());

        if (request.firstName() != null) {
            trainer.getUser().setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            trainer.getUser().setLastName(request.lastName());
        }
        if(request.active() != null) {
            trainer.setActive(request.active());
        }
        if (request.specialization() != null) {
            trainer.setSpecialization(TrainingType.fromEnum(TrainingType.Type.fromName(request.specialization())));
        }

        trainerDao.updateTrainer(trainer);

        List<Trainee> trainees = trainerDao.findAssignedTrainees(request.username());
        return toView(trainer, trainees);
    }

    // -------------------------------------------------------------------------
    // PASSWORD
    // -------------------------------------------------------------------------

    @Override
    public Boolean changePassword(@Valid PasswordChangeRequest request) {
        Trainer trainer = findTrainerOrThrow(request.username());
        // Verify old password
        if (!trainer.getUser().getPassword().equals(request.oldPassword())) {
            throw new InvalidCredentialsException("Old password is incorrect");
        }
        log.warn("Changing password of trainer '{}'", request.username());
        trainer.getUser().setPassword(request.newPassword());
        userDao.merge(trainer.getUser());
        return true;
    }

    // -------------------------------------------------------------------------
    // ACTIVATE / DEACTIVATE
    // -------------------------------------------------------------------------

    @Override
    public ActivationResponse activate(@Valid ActivationRequest request) {
        Trainer trainer = findTrainerOrThrow(request.username());
        log.info("Activating trainer '{}'", request.username());
        trainer.getUser().setActive(true);
        return new ActivationResponse(true);
    }

    @Override
    public ActivationResponse deactivate(@Valid ActivationRequest request) {
        Trainer trainer = findTrainerOrThrow(request.username());
        log.warn("Deactivating trainer '{}'", request.username());
        trainer.getUser().setActive(false);
        return new ActivationResponse(false);
    }

    // -------------------------------------------------------------------------
    // TRAININGS
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<TrainingView> getTrainings(@Valid TrainingSearchRequestForTrainer request) {
        findTrainerOrThrow(request.trainerUsername());

        log.info("Getting trainings for trainer '{}'", request.trainerUsername());

        List<Training> trainings =
                trainingDao.findForTrainer(
                        request.trainerUsername(),
                        request.fromDate(),
                        request.toDate(),
                        request.traineeUsername()
                );

        return trainings.stream()
                .map(viewMapper::toView)
                .toList();
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private Trainer findTrainerOrThrow(String username) {
        Trainer trainer = trainerDao.getTrainer(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found"));
        return trainer;
    }

    private TrainerView toView(Trainer trainer) {
        return viewMapper.toView(trainer);
    }


    private TrainerWithListView toView(Trainer trainer, List<Trainee> trainees) {
        return new TrainerWithListView(
                trainer.getUsername(), trainer.getFirstName(), trainer.getLastName(),
                trainer.isActive(), trainer.getSpecialization().getType().getName(),
                toUserInfoList(trainees)
        );
    }

    private List<UserInfo> toUserInfoList(List<Trainee> trainees) {
        return trainees.stream().map(this::toUserInfo).toList();
    }

    private UserInfo toUserInfo(Trainee trainee) {
        return new UserInfo(
                trainee.getUsername(), trainee.getFirstName(), trainee.getLastName()
        );
    }
}
