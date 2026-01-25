package dev.ilkersahin.java.spring.gym.xmlfileIO;

import dev.ilkersahin.java.spring.gym.service.TraineeService;
import dev.ilkersahin.java.spring.gym.service.TrainerService;
import dev.ilkersahin.java.spring.gym.service.TrainingService;
import dev.ilkersahin.java.spring.gym.xmlfileIO.dto.FileContentXml;
import dev.ilkersahin.java.spring.gym.xmlfileIO.mapper.TraineeXmlMapper;
import dev.ilkersahin.java.spring.gym.xmlfileIO.mapper.TrainerXmlMapper;
import dev.ilkersahin.java.spring.gym.xmlfileIO.mapper.TrainingXmlMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.Unmarshaller;
import org.springframework.stereotype.Component;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;

@Component
@Profile("xml-read")
// NOTE: Passwords are written in plain text for demo/import purposes only
public class XmlExternalFileReader {

    private static final Logger log = LoggerFactory.getLogger(XmlExternalFileReader.class);

    @Value("${gymapp.file.storage.path}")
    private String path;

    private Unmarshaller unmarshaller;
    private ResourceLoader resourceLoader;

    private TraineeService traineeService;
    private TrainerService trainerService;
    private TrainingService trainingService;

    public XmlExternalFileReader(Unmarshaller unmarshaller, ResourceLoader resourceLoader, TraineeService traineeService, TrainerService trainerService, TrainingService trainingService) {
        this.unmarshaller = unmarshaller;
        this.resourceLoader = resourceLoader;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    @PostConstruct
    void readFromXml() {

        log.info("xml-read profile active → reading data from file");
        log.info("Loading XML from {}", path);

        Resource resource = resourceLoader.getResource(path);

        FileContentXml root;
        try (InputStream inputStream = resource.getInputStream()) {
            root = (FileContentXml) unmarshaller.unmarshal(new StreamSource(inputStream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        root.getTrainers().stream()
                .map(TrainerXmlMapper::toDomain)
                .forEach(trainerService::createTrainer);

        root.getTrainees().stream()
                .map(TraineeXmlMapper::toDomain)
                .forEach(traineeService::createTrainee);

        root.getTrainings().stream()
                .map(TrainingXmlMapper::toDomain)
                .forEach(trainingService::createTraining);

        log.info("Loaded {} trainers, {} trainees, {} trainings",
                root.getTrainers().size(),
                root.getTrainees().size(),
                root.getTrainings().size()
        );
    }
}
