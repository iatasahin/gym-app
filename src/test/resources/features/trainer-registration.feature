Feature: Trainer Registration
  Trainers can register without authentication.
  Registration requires a specialization from the predefined training types.

  Background:
    Given the database is clean

  # --- Happy path ---

  Scenario: Register a trainer with a valid specialization
    When I register a trainer with:
      | firstName | lastName | specialization |
      | Mike      | Tyson    | Fitness        |
    Then the response status is 201
    And the response contains username "Mike.Tyson"
    And the response contains a password of length 10

  Scenario: Register trainers with different specializations
    When I register a trainer with:
      | firstName | lastName | specialization |
      | Anna      | Yoga     | Yoga           |
    Then the response status is 201
    When I register a trainer with:
      | firstName | lastName | specialization |
      | Bob       | Zumba    | Zumba          |
    Then the response status is 201

  # --- Username uniqueness ---

  Scenario: Duplicate trainer names get sequential suffixes
    When I register a trainer with:
      | firstName | lastName | specialization |
      | Mike      | Tyson    | Fitness        |
    Then the response contains username "Mike.Tyson"
    When I register a trainer with:
      | firstName | lastName | specialization |
      | Mike      | Tyson    | Yoga           |
    Then the response contains username "Mike.Tyson2"

  # --- Validation ---

  Scenario: Registration fails without first name
    When I register a trainer with:
      | firstName | lastName | specialization |
      |           | Tyson    | Fitness        |
    Then the response status is 400

  Scenario: Registration fails without specialization
    When I register a trainer with:
      | firstName | lastName | specialization |
      | Mike      | Tyson    |                |
    Then the response status is 400

  Scenario: Registration fails with invalid specialization
    When I register a trainer with:
      | firstName | lastName | specialization |
      | Mike      | Tyson    | Boxing         |
    Then the response status is 400