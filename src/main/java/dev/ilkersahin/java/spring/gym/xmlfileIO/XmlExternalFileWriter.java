package dev.ilkersahin.java.spring.gym.xmlfileIO;

import dev.ilkersahin.java.spring.gym.dto.request.TrainingCreateRequest;
import dev.ilkersahin.java.spring.gym.dto.response.TraineeCreateResponse;
import dev.ilkersahin.java.spring.gym.dto.response.TrainerCreateResponse;
import dev.ilkersahin.java.spring.gym.dto.view.TraineeView;
import dev.ilkersahin.java.spring.gym.dto.view.TrainerView;
import dev.ilkersahin.java.spring.gym.xmlfileIO.dto.FileContentXml;
import dev.ilkersahin.java.spring.gym.xmlfileIO.dto.TraineeXml;
import dev.ilkersahin.java.spring.gym.xmlfileIO.dto.TrainerXml;
import dev.ilkersahin.java.spring.gym.xmlfileIO.dto.TrainingXml;
import dev.ilkersahin.java.spring.gym.xmlfileIO.mapper.TraineeXmlMapper;
import dev.ilkersahin.java.spring.gym.xmlfileIO.mapper.TrainerXmlMapper;
import dev.ilkersahin.java.spring.gym.xmlfileIO.mapper.TrainingXmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
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
@Profile("xml-write")
// NOTE: Passwords are written in plain text for demo/import purposes only
public class XmlExternalFileWriter {

    private static final Logger log = LoggerFactory.getLogger(XmlExternalFileWriter.class);

    @Value("${gymapp.file.storage.path}")
    private String path;

    private final Marshaller marshaller;
    private final ResourceLoader resourceLoader;

    public XmlExternalFileWriter(Marshaller marshaller, ResourceLoader resourceLoader) {
        this.marshaller = marshaller;
        this.resourceLoader = resourceLoader;
    }

    void writeToXml(
            List<TrainerCreateResponse> trainers,
            List<TraineeCreateResponse> trainees,
            List<TrainingCreateRequest> trainings
    ) {
        log.info("Writing XML to {}", path);
        log.debug("Trainers: {}", trainers.stream()
                .map(TrainerCreateResponse::trainer)
                .map(TrainerView::username)
                .toList());
        log.debug("Trainees: {}", trainees.stream()
                .map(TraineeCreateResponse::trainee)
                .map(TraineeView::username)
                .toList());
        log.debug("Trainings: {}", trainings.stream()
                .map(t ->
                        t.trainingName() + " " + t.trainingDate() + " " + t.credentials().username())
                .toList());

        List<TrainerXml> trainerXmls = trainers.stream().map(TrainerXmlMapper::toXml).toList();
        List<TraineeXml> traineeXmls = trainees.stream().map(TraineeXmlMapper::toXml).toList();
        List<TrainingXml> trainingXmls = trainings.stream().map(TrainingXmlMapper::toXml).toList();

        FileContentXml root = new FileContentXml();
        root.setTrainers(trainerXmls);
        root.setTrainees(traineeXmls);
        root.setTrainings(trainingXmls);

        Resource resource = resourceLoader.getResource(path);

        if (!(resource instanceof WritableResource writable)) {
            log.error("File at {} is not writable", path);
            throw new IllegalStateException("Resource is not writable: " + path);
        }

        try (OutputStream outputStream = writable.getOutputStream()) {
            marshaller.marshal(root, new StreamResult(outputStream));
        } catch (IOException e) {
            log.error("Failed to write XML at {}", path);
            throw new RuntimeException("Failed to write XML", e);
        }
    }
}
