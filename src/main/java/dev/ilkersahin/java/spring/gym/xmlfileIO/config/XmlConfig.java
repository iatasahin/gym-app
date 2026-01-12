package dev.ilkersahin.java.spring.gym.xmlfileIO.config;

import dev.ilkersahin.java.spring.gym.xmlfileIO.dto.FileContentXml;
import dev.ilkersahin.java.spring.gym.xmlfileIO.dto.TraineeXml;
import dev.ilkersahin.java.spring.gym.xmlfileIO.dto.TrainerXml;
import dev.ilkersahin.java.spring.gym.xmlfileIO.dto.TrainingXml;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class XmlConfig {

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller m = new Jaxb2Marshaller();
        m.setClassesToBeBound(
                FileContentXml.class,
                TrainerXml.class,
                TraineeXml.class,
                TrainingXml.class
        );
        return m;
    }

}
