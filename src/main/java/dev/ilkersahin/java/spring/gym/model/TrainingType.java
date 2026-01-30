package dev.ilkersahin.java.spring.gym.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Table(name = "training_types",
        indexes = {
                @Index(name = "idx_training_type_name", columnList = "training_type_name")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TrainingType {
    @Id
    @Column(name = "training_type_id")
    private Integer trainingTypeId;

    @Column(name = "training_type_name", unique = true, nullable = false, length = 50)
    private String trainingTypeName;

    @Getter
    public enum Type {
        FITNESS(1, "Fitness"),
        YOGA(2, "Yoga"),
        ZUMBA(3, "Zumba"),
        STRETCHING(4, "Stretching"),
        RESISTANCE(5, "Resistance");

        private final int id;
        private final String name;

        Type(int id, String name) {
            this.name = name;
            this.id = id;
        }

        public static Type fromId(int id) {
            for (Type type : values()) {
                if (type.id == id) return type;
            }
            throw new IllegalArgumentException("Unknown TrainingType id: " + id);
        }

        public static Type fromName(String name) {
            for (Type type : values()) {
                if (type.name.equals(name)) return type;
            }
            throw new IllegalArgumentException("Unknown TrainingType name: " + name);
        }
    }

    public static TrainingType fromEnum(Type type) {
        return new TrainingType(type.getId(), type.getName());
    }

    public Type toEnum() {
        return Type.fromId(this.trainingTypeId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainingType that = (TrainingType) o;
        return Objects.equals(trainingTypeId, that.trainingTypeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trainingTypeId);
    }

    @Override
    public String toString() {
        return "TrainingType{" +
                "trainingTypeId=" + trainingTypeId +
                ", trainingTypeName='" + trainingTypeName + '\'' +
                '}';
    }
}
