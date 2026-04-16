Feature: Get Trainer Trainings
  Trainers can search their own training sessions with optional filters.
  Filters: date range, trainee name.

  Background:
    Given the database is clean
    And a registered trainee with:
      | firstName | lastName |
      | John      | Doe      |
    And a registered trainee with:
      | firstName | lastName |
      | Alice     | Brown    |
    And a registered trainer with:
      | firstName | lastName | specialization |
      | Jane      | Smith    | Fitness        |
    And I am logged in as "John.Doe"
    And the following trainings exist:
      | traineeUsername | trainerUsername | trainingName | trainingType | trainingDate | durationMinutes |
      | John.Doe        | Jane.Smith     | Cardio AM    | Fitness      | 2024-03-01   | 60              |
      | John.Doe        | Jane.Smith     | Cardio PM    | Fitness      | 2024-03-15   | 45              |
      | Alice.Brown     | Jane.Smith     | Intro Class  | Fitness      | 2024-04-01   | 30              |
    And I am logged in as "Jane.Smith"

  # --- No filters ---

  Scenario: Get all trainings for trainer
    When I get trainer trainings for "Jane.Smith"
    Then the response status is 200
    And the response should contain 3 trainings

  # --- Date range ---

  Scenario: Filter by date range
    When I get trainer trainings for "Jane.Smith" with filters:
      | fromDate | 2024-03-01 |
      | toDate   | 2024-03-31 |
    Then the response status is 200
    And the response should contain 2 trainings

  # --- Trainee filter ---

  Scenario: Filter by trainee name
    When I get trainer trainings for "Jane.Smith" with filters:
      | traineeName | John.Doe |
    Then the response status is 200
    And the response should contain 2 trainings

  Scenario: Filter by trainee with date range
    When I get trainer trainings for "Jane.Smith" with filters:
      | fromDate    | 2024-03-01 |
      | toDate      | 2024-03-31 |
      | traineeName | John.Doe   |
    Then the response status is 200
    And the response should contain 2 trainings

  Scenario: Filters that match nothing
    When I get trainer trainings for "Jane.Smith" with filters:
      | fromDate | 2025-01-01 |
      | toDate   | 2025-12-31 |
    Then the response status is 200
    And the response should contain 0 trainings

  # --- @SelfService ---

  Scenario: Trainer cannot view another trainer's trainings
    Given a registered trainer with:
      | firstName | lastName | specialization |
      | Bob       | Jones    | Yoga           |
    And I am logged in as "Bob.Jones"
    When I get trainer trainings for "Jane.Smith" with filters:
      | fromDate | 2024-01-01 |
    Then the response status is 403