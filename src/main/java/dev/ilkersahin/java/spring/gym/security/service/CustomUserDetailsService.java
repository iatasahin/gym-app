package dev.ilkersahin.java.spring.gym.security.service;

import dev.ilkersahin.java.spring.gym.model.User;
import dev.ilkersahin.java.spring.gym.repository.UserRepository;
import dev.ilkersahin.java.spring.gym.security.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        List<SimpleGrantedAuthority> authorities = buildAuthorities(user);

        log.debug("User '{}' loaded with authorities: {}", username, authorities);

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isActive(),
                true,
                true,
                true,
                authorities
        );
    }

    private List<SimpleGrantedAuthority> buildAuthorities(User user) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if(user.isTrainee()){
            authorities.add(new SimpleGrantedAuthority("ROLE_" + Role.TRAINEE.name()));
        }
        if(user.isTrainer()){
            authorities.add(new SimpleGrantedAuthority("ROLE_" + Role.TRAINER.name()));
        }
        if (authorities.isEmpty()) {
            log.warn("User '{}' has no trainee/trainer association", user.getUsername());
            throw new IllegalStateException(
                    "User '%s' has no trainee/trainer association".formatted(user.getUsername())
            );
        }
        return authorities;
    }

    public Role getPrimaryRole(User user) {
        if (user.isTrainer()) {
            return Role.TRAINER;
        }
        if (user.isTrainee()) {
            return Role.TRAINEE;
        }
        throw new IllegalStateException("User has no role assignment: " + user.getUsername());
    }
}
