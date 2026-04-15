Feature: Trainee Registration
  Trainees can self-register without authentication.
  The system generates a unique username and a random password.

  Background:
    Given the database is clean

  # --- Happy path ---

  Scenario: Register a trainee with minimal required fields
    When I register a trainee with:
      | firstName | lastName |
      | John      | Doe      |
    Then the response status is 201
    And the response contains username "John.Doe"
    And the response contains a password of length 10

  Scenario: Register a trainee with all fields
    When I register a trainee with:
      | firstName | lastName | dateOfBirth | address      |
      | Jane      | Smith    | 1995-06-15  | 123 Main St  |
    Then the response status is 201
    And the response contains username "Jane.Smith"

  # --- Username uniqueness ---

  Scenario: Duplicate names get sequential suffixes
    When I register a trainee with:
      | firstName | lastName |
      | John      | Doe      |
    Then the response contains username "John.Doe"
    When I register a trainee with:
      | firstName | lastName |
      | John      | Doe      |
    Then the response contains username "John.Doe2"
    When I register a trainee with:
      | firstName | lastName |
      | John      | Doe      |
    Then the response contains username "John.Doe3"

  # --- Validation ---

  Scenario: Registration fails without first name
    When I register a trainee with:
      | firstName | lastName |
      |           | Doe      |
    Then the response status is 400

  Scenario: Registration fails without last name
    When I register a trainee with:
      | firstName | lastName |
      | John      |          |
    Then the response status is 400
