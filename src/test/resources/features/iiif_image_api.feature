
Feature: IIIF Image Metadata and Transformation

  # Positive scenarios - successful metadata retrieval
  Scenario Outline: Retrieve IIIF image metadata successfully
    When I fetch IIIF metadata for object "<objectId>"
    Then the iiif response status should be 200
    And it should include width in pixels
    And it should include height in pixels

    Examples:
      | objectId |
      | RFwqO    |
      | ohGMs    |


  # Positive scenarios - successful image retrieval with transformations
  Scenario Outline: Retrieve IIIF image with valid parameters
    When I fetch IIIF image in format "<format>" for object "<objectId>" with region "<region>" and size "<size>" and rotation "<rotation>" and quality "<quality>"
    Then the iiif response status should be 200
    And the content type should be "<contentType>"

    Examples:
      | format | objectId | region | size   | rotation | quality | contentType  |
      | png    | RFwqO    | full   | max    | 0        | default | image/png    |
      | jpg    | RFwqO    | full   | max    | 0        | gray    | image/jpeg   |
      | jpg    | ohGMs    | full   | 800,80 | 90       | gray    | image/jpeg   |


  # Negative scenarios - metadata retrieval failures
  Scenario: Retrieve IIIF image metadata failure for invalid objects
    When I fetch IIIF metadata for object "invalid"
    Then the iiif response status should be 404


  # Negative scenarios - invalid image retrieval requests
  Scenario Outline: Retrieve IIIF image with invalid parameters
    When I fetch IIIF image in format "<format>" for object "<objectId>" with region "<region>" and size "<size>" and rotation "<rotation>" and quality "<quality>"
    Then the iiif response status should be <status>
    And the iiif error message should contain "<errorMessage>"

    Examples:
      | format | objectId | region | size | rotation | quality | status | errorMessage           |
      | bmp    | RFwqO    | full   | max  | 0        | default | 400    | [bmp] not supported    |
      | png    | RFwqO    | full   | max  | 999      | default | 400    | unsupported rotation   |

