Feature: Logout and Token Blacklisting
  Authenticated users can log out, which blacklists their JWT token.
  A blacklisted token is rejected on all subsequent requests.

  Background:
    Given the database is clean
    And a registered trainee with:
      | firstName | lastName |
      | John      | Doe      |
    And I am logged in as "John.Doe"

  Scenario: Successful logout
    When I logout
    Then the response status is 200
    And the response contains username "John.Doe"

  Scenario: Blacklisted token is rejected on subsequent requests
    When I logout
    Then the response status is 200
    When I request the profile for "John.Doe"
    Then the response status is 401

  Scenario: Fresh login works after logout
    When I logout
    Then the response status is 200
    When I log in as "John.Doe"
    And I request the profile for "John.Doe"
    Then the response status is 200

  Scenario: Logout without authentication
    When I logout without authentication
    Then the response status is 401
