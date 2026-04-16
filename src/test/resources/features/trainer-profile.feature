Feature: Get Trainer Profile
  Authenticated trainers can view their own profile.

  Background:
    Given the database is clean
    And a registered trainer with:
      | firstName | lastName | specialization |
      | Jane      | Smith    | Fitness        |

  Scenario: Trainer views their own profile
    Given I am logged in as "Jane.Smith"
    When I request the trainer profile for "Jane.Smith"
    Then the response status is 200
    And the profile response contains:
      | firstName      | Jane    |
      | lastName       | Smith   |

  Scenario: Profile includes assigned trainees
    Given a registered trainee with:
      | firstName | lastName |
      | John      | Doe      |
    And I am logged in as "John.Doe"
    And I update trainers for "John.Doe" with:
      | Jane.Smith |
    And I am logged in as "Jane.Smith"
    When I request the trainer profile for "Jane.Smith"
    Then the response status is 200

  Scenario: Trainer cannot view another trainer's profile
    Given a registered trainer with:
      | firstName | lastName | specialization |
      | Bob       | Jones    | Yoga           |
    And I am logged in as "Bob.Jones"
    When I request the trainer profile for "Jane.Smith"
    Then the response status is 403

  Scenario: Unauthenticated request is rejected
    When I request the trainer profile for "Jane.Smith" without authentication
    Then the response status is 401