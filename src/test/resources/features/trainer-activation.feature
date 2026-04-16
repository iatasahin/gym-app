Feature: Trainer Activation / Deactivation
  Trainers can activate or deactivate their own account.
  The operation is non-idempotent: repeating the same status change is a conflict.

  Background:
    Given the database is clean
    And a registered trainer with:
      | firstName | lastName | specialization |
      | Jane      | Smith    | Fitness        |
    And I am logged in as "Jane.Smith"

  Scenario: Deactivate an active trainer
    When I set trainer activation status for "Jane.Smith" to inactive
    Then the response status is 200

  Scenario: Activate an inactive trainer
    When I set trainer activation status for "Jane.Smith" to inactive
    Then the response status is 200
    When I set trainer activation status for "Jane.Smith" to active
    Then the response status is 200

  Scenario: Activating an already active trainer returns conflict
    When I set trainer activation status for "Jane.Smith" to active
    Then the response status is 409

  Scenario: Deactivating an already inactive trainer returns conflict
    When I set trainer activation status for "Jane.Smith" to inactive
    Then the response status is 200
    When I set trainer activation status for "Jane.Smith" to inactive
    Then the response status is 409

  Scenario: Trainer cannot change another trainer's status
    Given a registered trainer with:
      | firstName | lastName | specialization |
      | Bob       | Jones    | Yoga           |
    When I set trainer activation status for "Bob.Jones" to inactive
    Then the response status is 403

  Scenario: Unauthenticated request is rejected
    When I set trainer activation status for "Jane.Smith" to inactive without authentication
    Then the response status is 401