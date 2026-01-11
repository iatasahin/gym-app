package dev.ilkersahin.java.spring.gym.xmlfileIO;

import dev.ilkersahin.java.spring.gym.service.TraineeService;
import dev.ilkersahin.java.spring.gym.service.TrainerService;
import dev.ilkersahin.java.spring.gym.service.TrainingService;
import dev.ilkersahin.java.spring.gym.xmlfileIO.dto.FileContentXml;
import dev.ilkersahin.java.spring.gym.xmlfileIO.mapper.TraineeXmlMapper;
import dev.ilkersahin.java.spring.gym.xmlfileIO.mapper.TrainerXmlMapper;
import dev.ilkersahin.java.spring.gym.xmlfileIO.mapper.TrainingXmlMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.Unmarshaller;
import org.springframework.stereotype.Component;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;

@Component
public class XmlExternalFileReader {

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

        System.out.println("trainerService = " + trainerService.getAllTrainers());
        System.out.println("traineeService = " + traineeService.getAllTrainees());
        System.out.println("trainingService = " + trainingService.getAllTrainings());

        System.out.println("------- Before file read -------");

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

        System.out.println("------- After file read --------");
        System.out.println("trainerService = " + trainerService.getAllTrainers());
        System.out.println("traineeService = " + traineeService.getAllTrainees());
        System.out.println("trainingService = " + trainingService.getAllTrainings());
    }
}
