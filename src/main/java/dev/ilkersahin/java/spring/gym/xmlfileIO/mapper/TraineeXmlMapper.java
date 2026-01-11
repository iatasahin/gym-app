package dev.ilkersahin.java.spring.gym.xmlfileIO.mapper;

import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.xmlfileIO.dto.TraineeXml;

public class TraineeXmlMapper {

    public static TraineeXml toXml(Trainee t) {
        TraineeXml x = new TraineeXml();

        x.setFirstName(t.getFirstName());
        x.setLastName(t.getLastName());
        x.setUsername(t.getUsername());
        x.setPassword(t.getPassword());
        x.setActive(t.isActive());

        x.setDateOfBirth(t.getDateOfBirth());
        x.setAddress(t.getAddress());
        x.setUserId(t.getUserId());

        return x;
    }

    public static Trainee toDomain(TraineeXml x) {
        Trainee t = new Trainee();

        t.setFirstName(x.getFirstName());
        t.setLastName(x.getLastName());
        t.setUsername(x.getUsername());
        t.setPassword(x.getPassword());
        t.setActive(x.isActive());

        t.setDateOfBirth(x.getDateOfBirth());
        t.setAddress(x.getAddress());
        t.setUserId(x.getUserId());

        return t;
    }

}
