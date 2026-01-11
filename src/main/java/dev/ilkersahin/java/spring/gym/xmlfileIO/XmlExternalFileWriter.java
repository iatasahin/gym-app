package dev.ilkersahin.java.spring.gym.xmlfileIO;

import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.model.Training;
import dev.ilkersahin.java.spring.gym.xmlfileIO.dto.FileContentXml;
import dev.ilkersahin.java.spring.gym.xmlfileIO.dto.TraineeXml;
import dev.ilkersahin.java.spring.gym.xmlfileIO.dto.TrainerXml;
import dev.ilkersahin.java.spring.gym.xmlfileIO.dto.TrainingXml;
import dev.ilkersahin.java.spring.gym.xmlfileIO.mapper.TraineeXmlMapper;
import dev.ilkersahin.java.spring.gym.xmlfileIO.mapper.TrainerXmlMapper;
import dev.ilkersahin.java.spring.gym.xmlfileIO.mapper.TrainingXmlMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;
import org.springframework.oxm.Marshaller;
import org.springframework.stereotype.Component;

import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Component
public class XmlExternalFileWriter {

    @Value("${gymapp.file.storage.path}")
    private String path;

    private final Marshaller marshaller;
    private final ResourceLoader resourceLoader;

    public XmlExternalFileWriter(Marshaller marshaller, ResourceLoader resourceLoader) {
        this.marshaller = marshaller;
        this.resourceLoader = resourceLoader;
    }

    void writeToXml(List<Trainer> trainers, List<Trainee> trainees, List<Training> trainings) {

        List<TrainerXml> trainerXmls = trainers.stream().map(TrainerXmlMapper::toXml).toList();
        List<TraineeXml> traineeXmls = trainees.stream().map(TraineeXmlMapper::toXml).toList();
        List<TrainingXml> trainingXmls = trainings.stream().map(TrainingXmlMapper::toXml).toList();

        FileContentXml root = new FileContentXml();
        root.setTrainers(trainerXmls);
        root.setTrainees(traineeXmls);
        root.setTrainings(trainingXmls);

        Resource resource = resourceLoader.getResource(path);

        if(!(resource instanceof WritableResource writable)){
            throw new IllegalStateException(
                    "Resource is not writable: " + path
            );
        }

        try(OutputStream outputStream = writable.getOutputStream()) {
            marshaller.marshal(root, new StreamResult(outputStream));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write XML", e);
        }
    }
}
