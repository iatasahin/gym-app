Feature: Delete Trainee Profile
  Trainees can delete their own account.
  Deletion removes the trainee and associated data.

  Background:
    Given the database is clean
    And a registered trainee with:
      | firstName | lastName |
      | John      | Doe      |
    And I am logged in as "John.Doe"

  Scenario: Delete own profile
    When I delete trainee "John.Doe"
    Then the response status is 200

  Scenario: Profile is gone after deletion
    When I delete trainee "John.Doe"
    Then the response status is 200
    When I request the profile for "John.Doe"
    Then the response status is 404

  Scenario: Delete trainee with trainings
    Given a registered trainer with:
      | firstName | lastName | specialization |
      | Jane      | Smith    | Fitness        |
    And the following trainings exist:
      | traineeUsername | trainerUsername | trainingName | trainingType | trainingDate | durationMinutes |
      | John.Doe        | Jane.Smith     | Cardio AM    | Fitness      | 2024-03-01   | 60              |
    When I delete trainee "John.Doe"
    Then the response status is 200

  Scenario: Cannot delete another trainee's profile
    Given a registered trainee with:
      | firstName | lastName |
      | Jane      | Smith    |
    And I am logged in as "Jane.Smith"
    When I delete trainee "John.Doe"
    Then the response status is 403

  Scenario: Delete without authentication
    When I delete trainee "John.Doe" without authentication
    Then the response status is 401