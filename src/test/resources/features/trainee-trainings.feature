Feature: Get Trainee Trainings
  Trainees can search their own training sessions with optional filters.
  Filters: date range, trainer name, training type.

  Background:
    Given the database is clean
    And a registered trainee with:
      | firstName | lastName |
      | John      | Doe      |
    And a registered trainer with:
      | firstName | lastName | specialization |
      | Jane      | Smith    | Fitness        |
    And a registered trainer with:
      | firstName | lastName | specialization |
      | Bob       | Jones    | Yoga           |
    And I am logged in as "John.Doe"
    And the following trainings exist:
      | traineeUsername | trainerUsername | trainingName | trainingType | trainingDate | durationMinutes |
      | John.Doe        | Jane.Smith      | Cardio AM    | Fitness      | 2024-03-01   | 60              |
      | John.Doe        | Jane.Smith      | Cardio PM    | Fitness      | 2024-03-15   | 45              |
      | John.Doe        | Bob.Jones       | Yoga Class   | Yoga         | 2024-04-01   | 90              |

  # --- No filters ---

  Scenario: Get all trainings
    When I get trainings for "John.Doe"
    Then the response status is 200
    And the response should contain 3 trainings

  # --- Date range filter ---

  Scenario: Filter by date range
    When I get trainings for "John.Doe" with filters:
      | fromDate | 2024-03-01 |
      | toDate   | 2024-03-31 |
    Then the response status is 200
    And the response should contain 2 trainings

  Scenario: Filter with fromDate only
    When I get trainings for "John.Doe" with filters:
      | fromDate | 2024-04-01 |
    Then the response status is 200
    And the response should contain 1 training

  # --- Trainer filter ---

  Scenario: Filter by trainer name
    When I get trainings for "John.Doe" with filters:
      | trainerName | Jane.Smith |
    Then the response status is 200
    And the response should contain 2 trainings

  # --- Training type filter ---

  Scenario: Filter by training type
    When I get trainings for "John.Doe" with filters:
      | trainingType | Yoga |
    Then the response status is 200
    And the response should contain 1 training

  # --- Combined filters ---

  Scenario: Combine date range and trainer filter
    When I get trainings for "John.Doe" with filters:
      | fromDate    | 2024-03-01 |
      | toDate      | 2024-03-31 |
      | trainerName | Jane.Smith |
    Then the response status is 200
    And the response should contain 2 trainings

  Scenario: Filters that match nothing return empty list
    When I get trainings for "John.Doe" with filters:
      | fromDate | 2025-01-01 |
      | toDate   | 2025-12-31 |
    Then the response status is 200
    And the response should contain 0 trainings

  # --- @SelfService ---

  Scenario: Cannot view another trainee's trainings
    Given a registered trainee with:
      | firstName | lastName |
      | Jane      | Doe      |
    And I am logged in as "Jane.Doe"
    When I get trainings for "John.Doe" with filters:
      | fromDate | 2024-01-01 |
    Then the response status is 403
