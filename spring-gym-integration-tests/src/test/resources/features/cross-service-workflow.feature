Feature: Cross-Service Training Workflow
  When a training is created in the gym service,
  the workload service receives and processes the message,
  updating the trainer's workload summary.

  Background:
    Given the databases are clean
    And a registered trainee in the gym system:
      | firstName | John |
      | lastName  | Doe  |
    And a registered trainer in the gym system:
      | firstName      | Jane    |
      | lastName       | Smith   |
      | specialization | Fitness |
    And I am authenticated as "John.Doe"

  Scenario: Creating a training updates trainer workload
    When I create a training session:
      | traineeUsername | stored:traineeUsername |
      | trainerUsername | stored:trainerUsername |
      | trainingName    | Morning Cardio         |
      | trainingType    | Fitness                |
      | trainingDate    | 2024-03-15             |
      | durationMinutes | 60                     |
    Then the gym service responds with status 200
    And the trainer "stored:trainerUsername" should have workload data
    And the trainer "stored:trainerUsername" workload should show 60 minutes for 2024-3

  Scenario: Multiple trainings accumulate in workload
    When I create a training session:
      | traineeUsername | stored:traineeUsername |
      | trainerUsername | stored:trainerUsername |
      | trainingName    | Morning Cardio         |
      | trainingType    | Fitness                |
      | trainingDate    | 2024-03-15             |
      | durationMinutes | 60                     |
    Then the gym service responds with status 200
    When I create a training session:
      | traineeUsername | stored:traineeUsername |
      | trainerUsername | stored:trainerUsername |
      | trainingName    | Evening Cardio         |
      | trainingType    | Fitness                |
      | trainingDate    | 2024-03-20             |
      | durationMinutes | 45                     |
    Then the gym service responds with status 200
    And the trainer "stored:trainerUsername" workload should show 105 minutes for 2024-3

  Scenario: Trainings in different months create separate entries
    When I create a training session:
      | traineeUsername | stored:traineeUsername |
      | trainerUsername | stored:trainerUsername |
      | trainingName    | March Session          |
      | trainingType    | Fitness                |
      | trainingDate    | 2024-03-15             |
      | durationMinutes | 60                     |
    And I create a training session:
      | traineeUsername | stored:traineeUsername |
      | trainerUsername | stored:trainerUsername |
      | trainingName    | April Session          |
      | trainingType    | Fitness                |
      | trainingDate    | 2024-04-10             |
      | durationMinutes | 90                     |
    Then the trainer "stored:trainerUsername" workload should show 60 minutes for 2024-3
    And the trainer "stored:trainerUsername" workload should show 90 minutes for 2024-4

  Scenario: Trainer has no workload before any trainings
    Then the trainer "stored:trainerUsername" should have no workload data