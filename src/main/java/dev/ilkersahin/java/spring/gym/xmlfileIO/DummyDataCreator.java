package dev.ilkersahin.java.spring.gym.xmlfileIO;

import dev.ilkersahin.java.spring.gym.dto.auth.Credentials;
import dev.ilkersahin.java.spring.gym.dto.request.TraineeCreateRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TrainerCreateRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TrainingCreateRequest;
import dev.ilkersahin.java.spring.gym.dto.response.TraineeCreateResponse;
import dev.ilkersahin.java.spring.gym.dto.response.TrainerCreateResponse;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import dev.ilkersahin.java.spring.gym.service.TraineeService;
import dev.ilkersahin.java.spring.gym.service.TrainerService;
import dev.ilkersahin.java.spring.gym.service.TrainingService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Profile("xml-write")
// NOTE: Passwords are written in plain text for demo/import purposes only
public class DummyDataCreator {
    private static final Logger log = LoggerFactory.getLogger(DummyDataCreator.class);

    private TraineeService traineeService;
    private TrainerService trainerService;
    private TrainingService trainingService;
    private XmlExternalFileWriter xmlExternalFileWriter;

    public DummyDataCreator(TraineeService traineeService, TrainerService trainerService, TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    @Autowired
    public void setFileWriter(XmlExternalFileWriter xmlExternalFileWriter) {
        this.xmlExternalFileWriter = xmlExternalFileWriter;
    }

    @PostConstruct
    public void initializeTheExternalDataFile() {

        log.info("xml-write profile active → generating dummy data");

        // --- Trainers ---
        TrainerCreateResponse trainer1 =
                trainerService.createTrainer(
                        new TrainerCreateRequest("Tom", "Smith", TrainingType.Type.RESISTANCE)
                );

        TrainerCreateResponse trainer2 =
                trainerService.createTrainer(
                        new TrainerCreateRequest("Tom", "Smith", TrainingType.Type.FITNESS)
                );

        // --- Trainees ---
        TraineeCreateResponse trainee1 =
                traineeService.createTrainee(
                        new TraineeCreateRequest(
                                "Jack", "Black",
                                LocalDate.of(1980, 12, 27),
                                "anAddress"
                        )
                );

        TraineeCreateResponse trainee2 =
                traineeService.createTrainee(
                        new TraineeCreateRequest(
                                "Tom", "Smith",
                                LocalDate.of(1990, 5, 16),
                                "anotherAddress"
                        )
                );

        // --- Trainings ---
        Credentials credentials1 = new Credentials(
                trainee1.trainee().username(),
                trainee1.password()
        );

        TrainingCreateRequest training1 = new TrainingCreateRequest(
                credentials1,
                trainer1.trainer().username(),
                "Resistance Training",
                TrainingType.Type.RESISTANCE,
                LocalDate.of(2025, 8, 24),
                49
        );
        TrainingCreateRequest training2 = new TrainingCreateRequest(
                credentials1,
                trainer2.trainer().username(),
                "Fitness Training",
                TrainingType.Type.FITNESS,
                LocalDate.of(2024, 7, 15),
                58

        );
        TrainingCreateRequest training3 = new TrainingCreateRequest(
                new Credentials(
                        trainee2.trainee().username(),
                        trainee2.password()
                ),
                trainer2.trainer().username(),
                "Another Training",
                TrainingType.Type.FITNESS,
                LocalDate.of(2023, 3, 28),
                85
        );

        trainingService.createTraining(training1);
        trainingService.createTraining(training2);
        trainingService.createTraining(training3);

        xmlExternalFileWriter.writeToXml(
                List.of(trainer1, trainer2),
                List.of(trainee1, trainee2),
                List.of(training1, training2, training3)
        );

        log.info("XML generation finished");
    }
}
