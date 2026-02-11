package dev.ilkersahin.java.spring.gym.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages = {"dev.ilkersahin.java.spring.gym"})
public class Config {
    private static final Logger log = LoggerFactory.getLogger(Config.class);

    @PostConstruct
    public void logConfigLoaded() {
        log.info("Application properties loaded");
    }
}
