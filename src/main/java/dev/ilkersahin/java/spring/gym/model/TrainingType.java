package dev.ilkersahin.java.spring.gym.model;

public enum TrainingType {
    FITNESS("Fitness", 1),
    YOGA("Yoga", 2),
    ZUMBA("Zumba", 3),
    STRETCHING("Stretching", 4),
    RESISTANCE("Resistance", 5);

    private final String trainingTypeName;
    private final int ID;

    TrainingType(String name, int ID) {
        this.trainingTypeName = name;
        this.ID = ID;
    }

    public String getTrainingTypeName() {
        return trainingTypeName;
    }

    public int getID() {
        return ID;
    }
}
