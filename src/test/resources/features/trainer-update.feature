Feature: Update Trainer Profile
  Trainers can update their own profile information.

  Background:
    Given the database is clean
    And a registered trainer with:
      | firstName | lastName | specialization |
      | Jane      | Smith    | Fitness        |
    And I am logged in as "Jane.Smith"

  Scenario: Update trainer name
    When I update trainer "Jane.Smith" with:
      | username   | firstName | lastName | specialization | active |
      | Jane.Smith | Janet     | Smith    | Fitness        | true   |
    Then the response status is 200
    And the profile response contains:
      | firstName | Janet |

  Scenario: Update trainer specialization
    When I update trainer "Jane.Smith" with:
      | username   | firstName | lastName | specialization | active |
      | Jane.Smith | Jane      | Smith    | Yoga           | true   |
    Then the response status is 200

  Scenario: Cannot update another trainer's profile
    Given a registered trainer with:
      | firstName | lastName | specialization |
      | Bob       | Jones    | Yoga           |
    And I am logged in as "Bob.Jones"
    When I update trainer "Jane.Smith" with:
      | username   | firstName | lastName | specialization | active |
      | Jane.Smith | Hacked    | Smith    | Fitness        | true   |
    Then the response status is 403

  Scenario: Update without authentication
    When I update trainer "Jane.Smith" without authentication:
      | username   | firstName | lastName | specialization | active |
      | Jane.Smith | Janet     | Smith    | Fitness        | true   |
    Then the response status is 401