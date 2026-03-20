package dev.ilkersahin.java.spring.gym.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class SpringGymEurekaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringGymEurekaApplication.class, args);
    }

}
