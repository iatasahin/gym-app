# src/test/resources/features/training-creation.feature
Feature: Training Creation
  Authenticated users can schedule training sessions.
  Each training sends a workload notification to the message queue.

  Background:
    Given the database is clean
    And a registered trainee with:
      | firstName | lastName |
      | John      | Doe      |
    And a registered trainer with:
      | firstName | lastName | specialization |
      | Jane      | Smith    | Fitness        |

  # --- Happy path ---

  Scenario: Create a training session
    Given I am logged in as "John.Doe"
    When I create a training with:
      | traineeUsername | trainerUsername | trainingName   | trainingType | trainingDate | durationMinutes |
      | John.Doe        | Jane.Smith      | Morning Cardio | Fitness      | 2024-03-01   | 60              |
    Then the response status is 200

  # --- Message verification ---

  Scenario: Training creation sends workload notification
    Given I am logged in as "John.Doe"
    When I create a training with:
      | traineeUsername | trainerUsername | trainingName   | trainingType | trainingDate | durationMinutes |
      | John.Doe        | Jane.Smith      | Morning Cardio | Fitness      | 2024-03-01   | 60              |
    Then a message should be on the "workload.queue" queue
    And the workload message should contain:
      | trainerUsername  | Jane.Smith |
      | trainerFirstName | Jane       |
      | trainerLastName  | Smith      |
      | actionType       | ADD        |
      | trainingDuration | 60         |

  # --- Error cases ---

  Scenario: Fails with non-existent trainee
    Given I am logged in as "Jane.Smith"
    When I create a training with:
      | traineeUsername | trainerUsername | trainingName   | trainingType | trainingDate | durationMinutes |
      | Nobody.Here     | Jane.Smith      | Morning Cardio | Fitness      | 2024-03-01   | 60              |
    Then the response status is 404

  Scenario: Fails with non-existent trainer
    Given I am logged in as "John.Doe"
    When I create a training with:
      | traineeUsername | trainerUsername | trainingName   | trainingType | trainingDate | durationMinutes |
      | John.Doe        | Nobody.Here     | Morning Cardio | Fitness      | 2024-03-01   | 60              |
    Then the response status is 404

  Scenario: Fails without authentication
    When I create a training without authentication:
      | traineeUsername | trainerUsername | trainingName   | trainingType | trainingDate | durationMinutes |
      | John.Doe        | Jane.Smith      | Morning Cardio | Fitness      | 2024-03-01   | 60              |
    Then the response status is 401

  Scenario: Invalid training type
    Given I am logged in as "John.Doe"
    When I create a training with:
      | traineeUsername | trainerUsername | trainingName | trainingType | trainingDate | durationMinutes |
      | John.Doe        | Jane.Smith      | Boxing Class | Boxing       | 2024-03-01   | 60              |
    Then the response status is 400
