package dev.ilkersahin.java.spring.gym.xmlfileIO.dto;

import dev.ilkersahin.java.spring.gym.xmlfileIO.adapter.LocalDateAdapter;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class TraineeXml {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private boolean active;
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate dateOfBirth;
    private String address;
}
