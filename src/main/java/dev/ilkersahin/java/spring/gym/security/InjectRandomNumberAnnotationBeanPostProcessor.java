package dev.ilkersahin.java.spring.gym.security;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
public class InjectRandomNumberAnnotationBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        for (Field field : bean.getClass().getDeclaredFields()) {
            if(field.isAnnotationPresent(InjectRandomNumber.class)) {
                var annotation = field.getAnnotation(InjectRandomNumber.class);
                int min = annotation.min();
                int max = annotation.max();
                int randomValue = (int) (Math.random() * (max - min + 1)) + min;
                field.setAccessible(true);
                try {
                    field.set(bean, randomValue);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject random number into field: " + field.getName(), e);
                } finally {
                    field.setAccessible(false);
                }
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        // No processing after initialization
        return bean;
    }
}
