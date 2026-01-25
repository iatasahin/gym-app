package dev.ilkersahin.java.spring.gym.xmlfileIO.dto;

import dev.ilkersahin.java.spring.gym.model.TrainingType;
import dev.ilkersahin.java.spring.gym.xmlfileIO.adapter.DurationAdapter;
import dev.ilkersahin.java.spring.gym.xmlfileIO.adapter.LocalDateAdapter;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class TrainingXml {
    private String traineeUsername;
    private String traineePassword;
    private String trainerUsername;
    private String trainingName;
    private TrainingType.Type trainingType;
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate trainingDate;
    @XmlJavaTypeAdapter(DurationAdapter.class)
    private Duration trainingDuration;
}
