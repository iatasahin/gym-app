package dev.ilkersahin.java.spring.gym;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringGymApplication {
    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger(SpringGymApplication.class);
        log.info("Starting SpringGymApplication");

        ApplicationContext ctx =
                new AnnotationConfigApplicationContext(
                        "dev.ilkersahin.java.spring.gym"
                );

        log.info("Spring context started with {} beans",
                ctx.getBeanDefinitionCount()
        );
    }
}
