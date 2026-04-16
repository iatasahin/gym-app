Feature: Trainer Change Password
  Trainers can change their own password.

  Background:
    Given the database is clean
    And a registered trainer with:
      | firstName | lastName | specialization |
      | Jane      | Smith    | Fitness        |
    And I am logged in as "Jane.Smith"

  Scenario: Change password successfully
    When I change password for trainer "Jane.Smith" to "NewSecurePass1"
    Then the response status is 200

  Scenario: Old password stops working after change
    When I change password for trainer "Jane.Smith" to "NewSecurePass1"
    Then the response status is 200
    When I log in as "Jane.Smith" with password "NewSecurePass1"
    Then the response status is 200
    When I log in as "Jane.Smith" with original password
    Then the response status is 401

  Scenario: Cannot change another trainer's password
    Given a registered trainer with:
      | firstName | lastName | specialization |
      | Bob       | Jones    | Yoga           |
    And I am logged in as "Bob.Jones"
    When I change password for trainer "Jane.Smith" to "HackedPass1"
    Then the response status is 403

  Scenario: Change password without authentication
    When I change password for trainer "Jane.Smith" to "NewSecurePass1" without authentication
    Then the response status is 401