package dev.ilkersahin.java.spring.gym.dao.util;

import dev.ilkersahin.java.spring.gym.model.util.UsernameCounter;

public interface UsernameCounterDao {
    public UsernameCounter findByBaseUsernameWithLock(String baseUsername);
    public void persist(UsernameCounter counter);
    public UsernameCounter merge(UsernameCounter counter);
}
