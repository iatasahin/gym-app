package dev.ilkersahin.java.spring.gym.service.util;

import dev.ilkersahin.java.spring.gym.exception.MaxUsernameSuffixRetriesExceededException;
import dev.ilkersahin.java.spring.gym.exception.UsernameExistsException;
import dev.ilkersahin.java.spring.gym.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class UserCreationService {
    private static final Logger log = LoggerFactory.getLogger(UserCreationService.class);

    @Value("${gymapp.username.suffix.max-retries}")
    private int maxSuffixRetriesForUsername;

    private final PasswordGeneratorService passwordGeneratorService;

    public UserCreationService(PasswordGeneratorService passwordGeneratorService) {
        this.passwordGeneratorService = passwordGeneratorService;
    }

    public void setMaxSuffixRetriesForUsername(int maxSuffixRetriesForUsername) {
        this.maxSuffixRetriesForUsername = maxSuffixRetriesForUsername;
    }

    public <T extends User> T createUser(T user, Function<T, T> persistFunction, String userType){

        log.info("Creating {}: {} {}",userType, user.getFirstName(), user.getLastName());

        user.setPassword(passwordGeneratorService.generate(10));

        String defaultUsername = user.getFirstName() + "." + user.getLastName();
        user.setUsername(defaultUsername);
        int usernameSerialSuffix = 2;

        while (true) {
            try {
                T saved = persistFunction.apply(user);
                log.info("{} created with username '{}'", userType, saved.getUsername());
                return saved;
            } catch (UsernameExistsException e) {

                if (usernameSerialSuffix > maxSuffixRetriesForUsername + 1) {
                    log.error("Maximum username retries for {} with Username '{}' exceeded the maximum of '{}'; aborting creation.",
                            userType, defaultUsername, maxSuffixRetriesForUsername);
                    throw new MaxUsernameSuffixRetriesExceededException(
                            "Maximum username retries for %s with Username '%s' exceeded the maximum of '%s'; aborting creation."
                                    .formatted(userType, defaultUsername, maxSuffixRetriesForUsername));
                }

                log.warn("{} with Username '{}' already exists — trying '{}{}'",
                        userType, user.getUsername(), defaultUsername, usernameSerialSuffix
                );

                user.setUsername(defaultUsername + usernameSerialSuffix);
                usernameSerialSuffix++;
            }
        }
    }
}
