package dev.ilkersahin.java.spring.gym.xmlfileIO.mapper;

import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.xmlfileIO.dto.TrainerXml;

public class TrainerXmlMapper {

    public static TrainerXml toXml(Trainer t) {
        TrainerXml x = new TrainerXml();

        x.setFirstName(t.getFirstName());
        x.setLastName(t.getLastName());
        x.setUsername(t.getUsername());
        x.setPassword(t.getPassword());
        x.setActive(t.isActive());

        x.setSpecialization(t.getSpecialization());
        x.setUserId(t.getUserId());

        return x;
    }

    public static Trainer toDomain(TrainerXml x) {
        Trainer t = new Trainer();

        t.setFirstName(x.getFirstName());
        t.setLastName(x.getLastName());
        t.setUsername(x.getUsername());
        t.setPassword(x.getPassword());
        t.setActive(x.isActive());

        t.setSpecialization(x.getSpecialization());
        t.setUserId(x.getUserId());

        return t;
    }

}
