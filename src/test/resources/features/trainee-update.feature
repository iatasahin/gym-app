Feature: Update Trainee Profile
  Trainees can update their own profile information.

  Background:
    Given the database is clean
    And a registered trainee with:
      | firstName | lastName | dateOfBirth | address     |
      | John      | Doe      | 1990-01-15  | 123 Main St |
    And I am logged in as "John.Doe"

  Scenario: Update all fields
    When I update trainee "John.Doe" with:
      | username  | firstName | lastName | dateOfBirth | address     | active |
      | John.Doe  | Johnny    | Doe      | 1990-01-15  | 456 Oak Ave | true   |
    Then the response status is 200
    And the profile response contains:
      | firstName | Johnny      |
      | address   | 456 Oak Ave |

  Scenario: Update only name
    When I update trainee "John.Doe" with:
      | username | firstName | lastName | dateOfBirth | address     | active |
      | John.Doe | Jane      | Doe      | 1990-01-15  | 123 Main St | true   |
    Then the response status is 200
    And the profile response contains:
      | firstName | Jane |

  Scenario: Cannot update another trainee's profile
    Given a registered trainee with:
      | firstName | lastName |
      | Jane      | Smith    |
    And I am logged in as "Jane.Smith"
    When I update trainee "John.Doe" with:
      | username | firstName | lastName | dateOfBirth | address     | active |
      | John.Doe | Hacked    | Doe      | 1990-01-15  | 123 Main St | true   |
    Then the response status is 403

  Scenario: Update without authentication
    When I update trainee "John.Doe" without authentication:
      | username | firstName | lastName | dateOfBirth | address     | active |
      | John.Doe | Johnny    | Doe      | 1990-01-15  | 456 Oak Ave | true   |
    Then the response status is 401
