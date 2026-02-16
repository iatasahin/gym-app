package dev.ilkersahin.java.spring.gym.service.util;

import dev.ilkersahin.java.spring.gym.model.util.UsernameCounter;
import dev.ilkersahin.java.spring.gym.repository.UserRepository;
import dev.ilkersahin.java.spring.gym.repository.util.UsernameCounterRepository;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UsernameGeneratorService {

    @Setter(onMethod_ = {@Autowired})
    private UsernameCounterRepository usernameCounterRepository;

    @Setter(onMethod_ = {@Autowired})
    private UserRepository userRepository;


    /**
     * Generate unique username using counter table approach
     *
     * @param firstName user's first name
     * @param lastName  user's last name
     * @return unique username (e.g., "Tom.Smith" or "Tom.Smith3")
     */
    @Transactional
    public String generateUniqueUsername(String firstName, String lastName) {
        String baseUsername = firstName + "." + lastName;

        log.debug("Generating username for base: {}", baseUsername);

        if (!userRepository.existsByUsername(baseUsername)) {
            log.debug("Base username available: {}", baseUsername);
            createCounterEntry(baseUsername);
            return baseUsername;
        }

        return generateUsernameWithSuffix(baseUsername);
    }

    private void createCounterEntry(String baseUsername) {
        UsernameCounter counter = new UsernameCounter(baseUsername);
        usernameCounterRepository.saveAndFlush(counter);
        log.debug("Created counter entry for: {} with initial suffix: {}",
                baseUsername, counter.getCurrentSuffix());
    }

    private String generateUsernameWithSuffix(String baseUsername) {
        UsernameCounter counter = usernameCounterRepository
                .findByBaseUsernameWithLock(baseUsername)
                .orElseGet(() -> {
                    log.debug("Creating new counter for: {}", baseUsername);
                    return usernameCounterRepository.saveAndFlush(new UsernameCounter(baseUsername));
                });

        int suffix = counter.getAndIncrementSuffix();
        String username = baseUsername + suffix;

        usernameCounterRepository.save(counter);

        log.debug("Generated username with suffix: {}", username);
        return username;
    }
}
