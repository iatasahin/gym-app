package dev.ilkersahin.java.spring.gym.xmlfileIO.dto;

import dev.ilkersahin.java.spring.gym.model.TrainingType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class TrainerXml {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private boolean active;
    private TrainingType.Type specialization;
}
