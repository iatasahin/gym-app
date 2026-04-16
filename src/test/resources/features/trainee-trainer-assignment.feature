Feature: Trainee Trainer List Assignment
  Trainees can update their assigned trainer list.
  This is a full replacement — the new list replaces the old one entirely.

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

  # --- Assignment ---

  Scenario: Assign trainers to a trainee
    When I update trainers for "John.Doe" with:
      | Jane.Smith |
      | Bob.Jones  |
    Then the response status is 200
    And the response should contain 2 trainers
    And the response should contain trainer "Jane.Smith"
    And the response should contain trainer "Bob.Jones"

  # --- Full replacement ---

  Scenario: Updating replaces the entire trainer list
    Given I update trainers for "John.Doe" with:
      | Jane.Smith |
      | Bob.Jones  |
    When I update trainers for "John.Doe" with:
      | Alice.Brown |
    Then the response status is 200
    And the response should contain 1 trainer
    And the response should contain trainer "Alice.Brown"

  Scenario: Assign all available trainers
    When I update trainers for "John.Doe" with:
      | Jane.Smith  |
      | Bob.Jones   |
      | Alice.Brown |
    Then the response status is 200
    And the response should contain 3 trainers

  # --- Validation ---

  Scenario: Empty trainer list is rejected
    When I update trainers for "John.Doe" with empty list
    Then the response status is 400

  # --- @SelfService ---

  Scenario: Trainee cannot update another trainee's trainer list
    Given a registered trainee with:
      | firstName | lastName |
      | Jane      | Doe      |
    And I am logged in as "Jane.Doe"
    When I update trainers for "John.Doe" with:
      | Jane.Smith |
    Then the response status is 403