package dev.ilkersahin.java.spring.gym.service.util;

import dev.ilkersahin.java.spring.gym.dao.UserDao;
import dev.ilkersahin.java.spring.gym.dao.util.UsernameCounterDao;
import dev.ilkersahin.java.spring.gym.model.util.UsernameCounter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UsernameGeneratorService {

    @Setter(onMethod_ = {@Autowired})
    private UsernameCounterDao usernameCounterDao;

    @Setter(onMethod_ = {@Autowired})
    private UserDao userDao;


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

        if (!userDao.existsByUsername(baseUsername)) {
            log.debug("Base username available: {}", baseUsername);
            createCounterEntry(baseUsername);
            return baseUsername;
        }

        return generateUsernameWithSuffix(baseUsername);
    }

    private void createCounterEntry(String baseUsername) {
        UsernameCounter counter = new UsernameCounter(baseUsername);
        usernameCounterDao.persist(counter);
        log.debug("Created counter entry for: {} with initial suffix: {}",
                baseUsername, counter.getCurrentSuffix());
    }

    private String generateUsernameWithSuffix(String baseUsername) {
        UsernameCounter counter = usernameCounterDao.findByBaseUsernameWithLock(baseUsername);

        if (counter == null) {
            counter = new UsernameCounter(baseUsername);
            usernameCounterDao.persist(counter);
            log.debug("Created new counter for: {}", baseUsername);
        }

        int suffix = counter.getAndIncrementSuffix();
        String username = baseUsername + suffix;

        usernameCounterDao.merge(counter);

        log.debug("Generated username with suffix: {}", username);
        return username;
    }
}
