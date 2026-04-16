package dev.ilkersahin.java.spring.gym.workload.integration.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.testcontainers.activemq.ArtemisContainer;
import org.testcontainers.mongodb.MongoDBContainer;

@TestConfiguration(proxyBeanMethods = false)
@Slf4j
@Profile("integration-test")
public class TestContainersConfig {
    @Bean
    @ServiceConnection
    public MongoDBContainer mongoDBContainer(){
        return new MongoDBContainer("mongo:7.0");
    }

    @Bean
    @ServiceConnection
    public ArtemisContainer artemisContainer(){
        return new ArtemisContainer("apache/activemq-artemis:2.44.0")
                .withUser("artemis")
                .withPassword("artemis");
    }
}
