Feature: Trainer Workload Service
  Processes workload messages from the main gym service.
  Provides REST endpoint to query trainer workload summaries.

  Background:
    Given the workload database is clean
    And I have a valid auth token

  # ===================================================================
  # JMS Message Processing - ADD
  # ===================================================================

  Scenario: ADD message creates trainer workload
    When a workload message is sent:
      | trainerUsername  | Jane.Smith |
      | trainerFirstName | Jane       |
      | trainerLastName  | Smith      |
      | isActive         | true       |
      | trainingDate     | 2024-03-15 |
      | trainingDuration | 60         |
      | actionType       | ADD        |
    Then the workload for "Jane.Smith" should exist
    And the workload response should contain:
      | trainerUsername   | Jane.Smith |
      | trainerFirstName  | Jane       |
      | trainerLastName   | Smith      |
      | trainerStatus     | true       |
    And the workload for year 2024 month 3 should have duration 60

  Scenario: Multiple ADD messages accumulate duration
    When a workload message is sent:
      | trainerUsername  | Jane.Smith |
      | trainerFirstName | Jane       |
      | trainerLastName  | Smith      |
      | isActive         | true       |
      | trainingDate     | 2024-03-15 |
      | trainingDuration | 60         |
      | actionType       | ADD        |
    And a workload message is sent:
      | trainerUsername  | Jane.Smith |
      | trainerFirstName | Jane       |
      | trainerLastName  | Smith      |
      | isActive         | true       |
      | trainingDate     | 2024-03-20 |
      | trainingDuration | 45         |
      | actionType       | ADD        |
    Then the workload for year 2024 month 3 should have duration 105

  Scenario: ADD messages for different months create separate entries
    When a workload message is sent:
      | trainerUsername  | Jane.Smith |
      | trainerFirstName | Jane       |
      | trainerLastName  | Smith      |
      | isActive         | true       |
      | trainingDate     | 2024-03-15 |
      | trainingDuration | 60         |
      | actionType       | ADD        |
    And a workload message is sent:
      | trainerUsername  | Jane.Smith |
      | trainerFirstName | Jane       |
      | trainerLastName  | Smith      |
      | isActive         | true       |
      | trainingDate     | 2024-04-10 |
      | trainingDuration | 90         |
      | actionType       | ADD        |
    Then the workload for year 2024 month 3 should have duration 60
    And the workload for year 2024 month 4 should have duration 90

  # ===================================================================
  # JMS Message Processing - DELETE
  # ===================================================================

  Scenario: DELETE message decreases duration
    Given a workload message is sent:
      | trainerUsername  | Jane.Smith |
      | trainerFirstName | Jane       |
      | trainerLastName  | Smith      |
      | isActive         | true       |
      | trainingDate     | 2024-03-15 |
      | trainingDuration | 60         |
      | actionType       | ADD        |
    When a workload message is sent:
      | trainerUsername  | Jane.Smith |
      | trainerFirstName | Jane       |
      | trainerLastName  | Smith      |
      | isActive         | true       |
      | trainingDate     | 2024-03-15 |
      | trainingDuration | 20         |
      | actionType       | DELETE     |
    Then the workload for year 2024 month 3 should have duration 40

  Scenario: DELETE does not go below zero
    Given a workload message is sent:
      | trainerUsername  | Jane.Smith |
      | trainerFirstName | Jane       |
      | trainerLastName  | Smith      |
      | isActive         | true       |
      | trainingDate     | 2024-03-15 |
      | trainingDuration | 30         |
      | actionType       | ADD        |
    When a workload message is sent:
      | trainerUsername  | Jane.Smith |
      | trainerFirstName | Jane       |
      | trainerLastName  | Smith      |
      | isActive         | true       |
      | trainingDate     | 2024-03-15 |
      | trainingDuration | 100        |
      | actionType       | DELETE     |
    Then the workload for year 2024 month 3 should have duration 0

  Scenario: DELETE for non-existent trainer is no-op
    When a workload message is sent:
      | trainerUsername  | Nobody.Here |
      | trainerFirstName | Nobody      |
      | trainerLastName  | Here        |
      | isActive         | true        |
      | trainingDate     | 2024-03-15  |
      | trainingDuration | 60          |
      | actionType       | DELETE      |
    Then the workload for "Nobody.Here" should not exist

  # ===================================================================
  # REST Endpoint
  # ===================================================================

  Scenario: Get workload for existing trainer
    Given a workload message is sent:
      | trainerUsername  | Jane.Smith |
      | trainerFirstName | Jane       |
      | trainerLastName  | Smith      |
      | isActive         | true       |
      | trainingDate     | 2024-03-15 |
      | trainingDuration | 60         |
      | actionType       | ADD        |
    When I request the workload for "Jane.Smith"
    Then the response status is 200

  Scenario: Get workload for non-existent trainer returns 404
    When I request the workload for "Nobody.Here"
    Then the response status is 404

  # ===================================================================
  # Authentication
  # ===================================================================

  Scenario: Unauthenticated request is rejected
    Given a workload message is sent:
      | trainerUsername  | Jane.Smith |
      | trainerFirstName | Jane       |
      | trainerLastName  | Smith      |
      | isActive         | true       |
      | trainingDate     | 2024-03-15 |
      | trainingDuration | 60         |
      | actionType       | ADD        |
    When I request the workload for "Jane.Smith" without authentication
    Then the response status is 403