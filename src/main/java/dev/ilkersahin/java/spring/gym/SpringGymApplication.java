package dev.ilkersahin.java.spring.gym;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringGymApplication {
    public static void main(String[] args) {
        ApplicationContext ctx =
                new AnnotationConfigApplicationContext(
                        "dev.ilkersahin.java.spring.gym"
                );
    }
}
