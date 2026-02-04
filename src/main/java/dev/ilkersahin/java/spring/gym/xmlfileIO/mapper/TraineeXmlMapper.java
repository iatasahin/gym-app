package dev.ilkersahin.java.spring.gym.xmlfileIO.mapper;

import dev.ilkersahin.java.spring.gym.dto.request.TraineeCreateRequest;
import dev.ilkersahin.java.spring.gym.dto.response.TraineeCreateResponse;
import dev.ilkersahin.java.spring.gym.xmlfileIO.dto.TraineeXml;

public class TraineeXmlMapper {
    public static TraineeXml toXml(TraineeCreateResponse r) {
        TraineeXml x = new TraineeXml();
        x.setFirstName(r.trainee().firstName());
        x.setLastName(r.trainee().lastName());
        x.setUsername(r.trainee().username());
        x.setPassword(r.password());
        x.setActive(r.trainee().active());
        x.setDateOfBirth(r.trainee().dateOfBirth());
        x.setAddress(r.trainee().address());
        return x;
    }

    public static TraineeCreateRequest toDomain(TraineeXml x) {
        return new TraineeCreateRequest(
                x.getFirstName(),
                x.getLastName(),
                x.getDateOfBirth(),
                x.getAddress()
        );
    }
}
