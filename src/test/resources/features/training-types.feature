Feature: Training Types
  The system provides a predefined list of training types.
  This endpoint is public — no authentication required.

  Scenario: Get all training types
    When I request the list of training types
    Then the response status is 200
    And the response should contain 5 training types
    And the response should contain training type "Fitness"
    And the response should contain training type "Yoga"
    And the response should contain training type "Zumba"
    And the response should contain training type "Stretching"
    And the response should contain training type "Resistance"