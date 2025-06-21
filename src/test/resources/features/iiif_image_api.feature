Feature: Rijksmuseum IIIF Image Metadata and Transformation

  Background:
    Given the IIIF image API base URL is set

  Scenario Outline: Retrieve IIIF image metadata successfully
    When I fetch IIIF metadata for object "<objectId>"
    Then the response status should be 200
    And It should include width and height in pixels

    Examples:
      | objectId |
      | RFwqO    |
      | ohGMs    |


  Scenario Outline: Retrieve IIIF image with valid parameters
    When I fetch IIIF image in format "<format>" for object "<objectId>" with region "<region>" and size "<size>" and rotation "<rotation>" and quality "<quality>"
    Then the response status should be 200
    And the content type should be "<contentType>"

    Examples:
      | format | objectId | region | size   | rotation | quality | contentType  |
      | png    | RFwqO    | full   | max    | 0        | default | image/png    |
      | jpg    | RFwqO    | full   | max    | 0        | gray    | image/jpeg   |
      | jpg    | ohGMs    | full   | 800,80 | 90       | gray    | image/jpeg   |


  Scenario: Retrieve IIIF image metadata failure for invalid objects
    When I fetch IIIF metadata for object "invalid"
    Then the response status should be 404


  Scenario Outline: Retrieve IIIF image with invalid parameters
    When I fetch IIIF image in format "<format>" for object "<objectId>" with region "<region>" and size "<size>" and rotation "<rotation>" and quality "<quality>"
    Then the response status should be <status>
    And the error message should contain "<errorMessage>"

    Examples:
      | format | objectId | region | size | rotation | quality | status | errorMessage           |
      | bmp    | RFwqO    | full   | max  | 0        | default | 400    | [bmp] not supported    |
      | png    | RFwqO    | full   | max  | 999      | default | 400    | unsupported rotation   |

