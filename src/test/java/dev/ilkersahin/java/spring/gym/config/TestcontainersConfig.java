package dev.ilkersahin.java.spring.gym.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;

@TestConfiguration(proxyBeanMethods = false)
@Slf4j
public class TestcontainersConfig {

    @Bean
    @ServiceConnection
    public MySQLContainer mysqlContainer(){
        return new MySQLContainer("mysql:9.5")
                .withDatabaseName("gymappDatabase")
                .withUsername("gymapp_user")
                .withPassword("gymapp_password");
    }
}
