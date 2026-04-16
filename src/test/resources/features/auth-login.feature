Feature: Login and Brute Force Protection
  Users authenticate with username and password.
  After repeated failures, the account is temporarily blocked.

  Background:
    Given the database is clean
    And a registered trainee with:
      | firstName | lastName |
      | John      | Doe      |

  # --- Login failures ---

  Scenario: Wrong password
    When I log in as "John.Doe" with password "WrongPassword"
    Then the response status is 401

  Scenario: Non-existent user
    When I log in as "Nobody.Here" with password "anything"
    Then the response status is 401

  Scenario: Empty credentials
    When I log in with empty credentials
    Then the response status is 400

  # --- Brute force protection ---

  Scenario: Account is blocked after repeated failed attempts
    When I fail to log in as "John.Doe" 3 times
    When I log in as "John.Doe" with password "WrongPassword"
    Then the response status is 429
    And the response body contains "Account temporarily locked due to multiple failed login attempts"

  Scenario: Malformed token is rejected
    When I request the profile for "John.Doe" with token "not-a-valid-jwt"
    Then the response status is 401
