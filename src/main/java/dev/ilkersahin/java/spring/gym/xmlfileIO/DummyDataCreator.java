package dev.ilkersahin.java.spring.gym.xmlfileIO;

import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.model.Training;
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

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@Component
@Profile("xml-write")
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
    public void initializeTheExternalDataFile(){

        log.info("xml-write profile active → generating dummy data");

        Trainer trainer1 = new Trainer(
                "Tom", "Smith",
                null, null,
                true, TrainingType.RESISTANCE, UUID.randomUUID()
        );
        Trainer trainer2 = new Trainer(
                "Tom", "Smith",
                null, null,
                true, TrainingType.FITNESS, UUID.randomUUID()
        );

        Trainee trainee1 = new Trainee(
                "Jack", "Black",
                null, null,
                true,
                LocalDate.of(1980, 12, 27),
                "anAddress", UUID.randomUUID()
        );
        Trainee trainee2 = new Trainee(
                "Tom", "Smith",
                null, null,
                true,
                LocalDate.of(1990, 5, 16),
                "anAddress", UUID.randomUUID()
        );

        Training training1 = new Training(
                trainee1.getUserId(), trainer1.getUserId(),
                "aTrainingName", TrainingType.RESISTANCE,
                LocalDate.of(2025,8,24),
                Duration.ofMinutes(49)
        );
        Training training2 = new Training(
                trainee1.getUserId(), trainer2.getUserId(),
                "aTrainingName", TrainingType.FITNESS,
                LocalDate.of(2024,7,15),
                Duration.ofMinutes(58)
        );
        Training training3 = new Training(
                trainee2.getUserId(), trainer2.getUserId(),
                "anotherTrainingName", TrainingType.FITNESS,
                LocalDate.of(2023,3,28),
                Duration.ofMinutes(85)
        );

        trainerService.createTrainer(trainer1);
        trainerService.createTrainer(trainer2);

        traineeService.createTrainee(trainee1);
        traineeService.createTrainee(trainee2);

        trainingService.createTraining(training1);
        trainingService.createTraining(training2);
        trainingService.createTraining(training3);

        log.info("Dummy data created");
        log.debug("Trainers: {}", trainerService.getAllTrainers().stream().map(Trainer::getUsername).toList());
        log.debug("Trainees: {}", traineeService.getAllTrainees().stream().map(Trainee::getUsername).toList());
        log.debug("Trainings: {}", trainingService.getAllTrainings());

        xmlExternalFileWriter.writeToXml(
                trainerService.getAllTrainers(),
                traineeService.getAllTrainees(),
                trainingService.getAllTrainings()
        );

        log.info("XML generation finished");
    }
}
