Feature: Rijksmuseum Search API Validation with Scenario Outlines

  Background:
    Given the Rijksmuseum API base URL is set

  Scenario Outline: Search artworks and validate object IDs contain the search criteria
    When I search artworks with parameters:
      | parameter | value   |
      | <parameter> | <value> |
    Then the response status should be 200
    And each response ID should contain parameters and values

    Examples:
      | parameter     | value              |
      | title         | Night Watch        |
      | creator       | Rembrandt van Rijn |
      | type          | painting           |
      | creationDate  | 16?                |



  Scenario Outline: Search tests for objectNumber field with wildcards
    When I search artworks with parameters:
      | parameter    | value     |
      | objectNumber | <value>   |
    Then the response status should be 200
    Then each response ID should contain parameters and values
    And the resolved objectNumbers should match pattern "<value>"

    Examples:
      | value   |
      | SK-C-5* |
      | SK-C-5  |
      | AB-C-50 |


  # Search with multiple parameters in one request, including duplicated parameters
  Scenario Outline: Search artworks by type, technique, and materials
    When I search artworks with parameters:
      | parameter | value       |
      | type      | <type>      |
      | technique | <technique> |
      | material  | <material1> |
      | material  | <material2> |
    Then the response status should be 200
    And each response ID should contain parameters and values

    Examples:
      | type     | technique  | material1 | material2 |
      | painting | embroidery | canvas    | oil paint |


  # Search by image availability boolean parameter and test pagination
  Scenario Outline: Search artworks by image availability and verify pagination
    When I search artworks with imageAvailable "<imageAvailable>"
    Then the response status should be 200
    When I request the next page using the pageToken from response
    Then the response status should be 200

    Examples:
      | imageAvailable |
      | true           |
      | false          |

  # Search by objectNumber and creator combined
  Scenario Outline: Search artworks by objectNumber and creator combination
    When I search artworks with parameters:
      | parameter    | value             |
      | objectNumber | <objectNumber>    |
      | creator      | <creator>         |
    Then the response status should be 200
    And each art object should have an id

    Examples:
      | objectNumber | creator            |
      | SK-C-5*      | Rembrandt van Rijn |

  # Negative test cases with invalid parameter values
  Scenario: Search artworks with invalid parameters should return errors
    When I search artworks with "invalidParam" and "invalidValue"
    Then the response status should be 400
    And the error message should contain "Unsupported query parameter"





