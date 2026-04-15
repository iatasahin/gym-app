Feature: Get Trainee Profile
  Authenticated trainees can view their own profile.
  The @SelfService policy prevents access to other trainees' data.

  Background:
    Given the trainee database is empty

  Scenario: Trainee views their own profile
    Given a registered trainee with:
      | firstName | lastName | dateOfBirth | address     |
      | John      | Doe      | 1990-01-15  | 123 Main St |
    And I am logged in as "John.Doe"
    When I request the profile for "John.Doe"
    Then the response status is 200
    And the profile response contains:
      | firstName   | John        |
      | lastName    | Doe         |
      | address     | 123 Main St |

  Scenario: Trainee cannot view another trainee's profile
    Given a registered trainee with:
      | firstName | lastName |
      | John      | Doe      |
    And a registered trainee with:
      | firstName | lastName |
      | Jane      | Smith    |
    And I am logged in as "John.Doe"
    When I request the profile for "Jane.Smith"
    Then the response status is 403

  Scenario: Unauthenticated request is rejected
    Given a registered trainee with:
      | firstName | lastName |
      | John      | Doe      |
    When I request the profile for "John.Doe" without authentication
    Then the response status is 401

  Scenario: Profile not found
    Given a registered trainee with:
      | firstName | lastName |
      | John      | Doe      |
    And I am logged in as "John.Doe"
    When I request the profile for "NonExistent.User"
    Then the response status is 403
