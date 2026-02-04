package dev.ilkersahin.java.spring.gym.model.util;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "username_counters")
@Getter
@Setter
@NoArgsConstructor
public class UsernameCounter {
    @Id
    @Column(name = "base_username", length = 255)
    private String baseUsername;

    @Column(name = "current_suffix", nullable = false)
    private Integer currentSuffix = 2;  // Start at 2 (first user has no suffix)

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public UsernameCounter(String baseUsername) {
        this.baseUsername = baseUsername;
        this.currentSuffix = 2;
    }

    public Integer getAndIncrementSuffix() {
        Integer current = this.currentSuffix;
        this.currentSuffix++;
        return current;
    }
}
