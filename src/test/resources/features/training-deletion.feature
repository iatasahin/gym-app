Feature: Training Deletion
  Authenticated users can delete training sessions.
  Each deletion sends a DELETE workload notification to the message queue.

  Background:
    Given the database is clean
    And a registered trainee with:
      | firstName | lastName |
      | John      | Doe      |
    And a registered trainer with:
      | firstName | lastName | specialization |
      | Jane      | Smith    | Fitness        |
    And I am logged in as "John.Doe"
    And a training exists:
      | traineeUsername | trainerUsername | trainingName   | trainingType | trainingDate | durationMinutes |
      | John.Doe        | Jane.Smith      | Morning Cardio | Fitness      | 2024-03-01   | 60              |

  Scenario: Delete a training session
    When I delete the training
    Then the response status is 200

  Scenario: Deletion sends DELETE workload notification
    When I delete the training
    Then a message should be on the "workload.queue" queue
    And the workload message should contain:
      | trainerUsername  | Jane.Smith |
      | actionType       | DELETE     |
      | trainingDuration | 60         |

  Scenario: Delete non-existent training returns 404
    When I delete training with id "00000000-0000-0000-0000-000000000000"
    Then the response status is 404

  Scenario: Delete without authentication returns 401
    When I delete the training without authentication
    Then the response status is 401
