package dev.ilkersahin.java.spring.gym.service.util;

import dev.ilkersahin.java.spring.gym.model.User;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class UserCreationService {
    private static final Logger log = LoggerFactory.getLogger(UserCreationService.class);

    private final PasswordGeneratorService passwordGeneratorService;

    @Setter(onMethod_ = {@Autowired})
    private UsernameGeneratorService usernameGeneratorService;

    public <T extends User> T createUser(T user, Function<T, T> persistFunction, String userType){

        log.info("Creating {}: {} {}",userType, user.getFirstName(), user.getLastName());

        user.setPassword(passwordGeneratorService.generate(10));
        user.setUsername(usernameGeneratorService.generateUniqueUsername(user.getFirstName(), user.getLastName()));

        T saved = persistFunction.apply(user);

        log.info("{} created with username '{}'", userType, saved.getUsername());
        return saved;
    }
}
