package dev.ilkersahin.java.spring.gym.xmlfileIO.mapper;

import dev.ilkersahin.java.spring.gym.dto.request.TrainerCreateRequest;
import dev.ilkersahin.java.spring.gym.dto.response.TrainerCreateResponse;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import dev.ilkersahin.java.spring.gym.xmlfileIO.dto.TrainerXml;

public class TrainerXmlMapper {
    public static TrainerXml toXml(TrainerCreateResponse r) {
        TrainerXml x = new TrainerXml();
        x.setFirstName(r.trainer().firstName());
        x.setLastName(r.trainer().lastName());
        x.setUsername(r.trainer().username());
        x.setPassword(r.password());
        x.setActive(r.trainer().active());
        x.setSpecialization(TrainingType.Type.fromName(r.trainer().specialization()));
        return x;
    }

    public static TrainerCreateRequest toDomain(TrainerXml x) {
        return new TrainerCreateRequest(
                x.getFirstName(),
                x.getLastName(),
                x.getSpecialization()
        );
    }
}
