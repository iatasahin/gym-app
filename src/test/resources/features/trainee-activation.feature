Feature: Trainee Activation / Deactivation
  Trainees can activate or deactivate their own account.
  The operation is non-idempotent: repeating the same status change is a conflict.

  Background:
    Given the database is clean
    And a registered trainee with:
      | firstName | lastName |
      | John      | Doe      |
    And I am logged in as "John.Doe"

  # --- New trainees are active by default ---

  Scenario: Deactivate an active trainee
    When I set activation status for "John.Doe" to inactive
    Then the response status is 200

  Scenario: Activate an inactive trainee
    When I set activation status for "John.Doe" to inactive
    Then the response status is 200
    When I set activation status for "John.Doe" to active
    Then the response status is 200

  # --- Non-idempotent behavior: 409 CONFLICT ---

  Scenario: Activating an already active trainee returns conflict
    When I set activation status for "John.Doe" to active
    Then the response status is 409

  Scenario: Deactivating an already inactive trainee returns conflict
    When I set activation status for "John.Doe" to inactive
    Then the response status is 200
    When I set activation status for "John.Doe" to inactive
    Then the response status is 409

  # --- @SelfService enforcement ---

  Scenario: Trainee cannot change another trainee's status
    Given a registered trainee with:
      | firstName | lastName |
      | Jane      | Smith    |
    When I set activation status for "Jane.Smith" to inactive
    Then the response status is 403

  Scenario: Unauthenticated request is rejected
    When I set activation status for "John.Doe" to inactive without authentication
    Then the response status is 401
