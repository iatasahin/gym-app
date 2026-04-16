Feature: Get Unassigned Trainers
  Trainees can see which trainers are not yet assigned to them.
  This helps trainees discover and add new trainers.

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
    And a registered trainer with:
      | firstName | lastName | specialization |
      | Alice     | Brown    | Zumba          |
    And I am logged in as "John.Doe"

  # --- No trainers assigned yet ---

  Scenario: All trainers are unassigned initially
    When I get unassigned trainers for "John.Doe"
    Then the response status is 200
    And the response should contain 3 trainers

  # --- After partial assignment ---

  Scenario: Assigned trainers are excluded
    Given I update trainers for "John.Doe" with:
      | Jane.Smith |
    When I get unassigned trainers for "John.Doe"
    Then the response status is 200
    And the response should contain 2 trainers
    And the response should contain trainer "Bob.Jones"
    And the response should contain trainer "Alice.Brown"

  Scenario: Assign all trainers leaves none unassigned
    Given I update trainers for "John.Doe" with:
      | Jane.Smith  |
      | Bob.Jones   |
      | Alice.Brown |
    When I get unassigned trainers for "John.Doe"
    Then the response status is 200
    And the response should contain 0 trainers

  # --- After reassignment ---

  Scenario: Reassigning trainers updates unassigned list
    Given I update trainers for "John.Doe" with:
      | Jane.Smith |
      | Bob.Jones  |
    When I get unassigned trainers for "John.Doe"
    Then the response should contain 1 trainer
    And the response should contain trainer "Alice.Brown"
    Given I update trainers for "John.Doe" with:
      | Alice.Brown |
    When I get unassigned trainers for "John.Doe"
    Then the response should contain 2 trainers
    And the response should contain trainer "Jane.Smith"
    And the response should contain trainer "Bob.Jones"

  # --- @SelfService ---

  Scenario: Cannot view another trainee's unassigned trainers
    Given a registered trainee with:
      | firstName | lastName |
      | Jane      | Doe      |
    And I am logged in as "Jane.Doe"
    When I get unassigned trainers for "John.Doe"
    Then the response status is 403

  Scenario: Unauthenticated request is rejected
    When I get unassigned trainers for "John.Doe" without authentication
    Then the response status is 401