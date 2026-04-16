Feature: Trainee Change Password
  Trainees can change their own password.
  After changing, the old password stops working and the new one works.

  Background:
    Given the database is clean
    And a registered trainee with:
      | firstName | lastName |
      | John      | Doe      |
    And I am logged in as "John.Doe"

  Scenario: Change password successfully
    When I change password for trainee "John.Doe" to "NewSecurePass1"
    Then the response status is 200

  Scenario: Old password stops working after change
    When I change password for trainee "John.Doe" to "NewSecurePass1"
    Then the response status is 200
    When I log in as "John.Doe" with password "NewSecurePass1"
    Then the response status is 200
    When I log in as "John.Doe" with original password
    Then the response status is 401

  Scenario: Cannot change another trainee's password
    Given a registered trainee with:
      | firstName | lastName |
      | Jane      | Smith    |
    And I am logged in as "Jane.Smith"
    When I change password for trainee "John.Doe" to "HackedPass1"
    Then the response status is 403

  Scenario: Change password without authentication
    When I change password for trainee "John.Doe" to "NewSecurePass1" without authentication
    Then the response status is 401