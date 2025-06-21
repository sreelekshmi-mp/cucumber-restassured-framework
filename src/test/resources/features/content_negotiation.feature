Feature: Rijksmuseum Content Negotiation with Query String Arguments

  Background:
    Given the Content negotiation API base URL is set


  Scenario Outline: Profile equal to specific object ID and profiles without media type returns correct profile data.
    When I send a GET request for object ID "<objectID>" with query parameter _profile="<profile>" and optional media type "<mediaType>"
    Then the response status should be 200
    And the response Content-Type should match "<expectedContentType>"
    And the response link header should contain "profile token" "<expectedTokens>"

    Examples:

    | objectID  | profile     | expectedContentType     | expectedTokens             |
    | 20024929  |  _alt       | application/json        | la,la-framed,oai_dc,edm    |
    | 200100988 | schema      | application/ld+json     | la,la-framed,oai_dc,edm    |


  Scenario Outline: Resolver returns expected profile, media type, and relation in the Link header
    When I send a GET request for object ID "200107928" with query parameter _profile="<profile>" and optional media type "<mediaType>"
    Then the response status should be 200
    And the response link header should contain "profile token" "<profile>"
    And the response link header should contain "media type" "<mediaType>"
    And the response link header should contain "relation" "<relation>"


    Examples:
      | profile    | mediaType               | relation  |
      | la         | application/n-triples   | alternate |
      | la-framed  | application/ld+json     | canonical |
      | oai_dc     | application/rdf+xml     | alternate |
      | edm        | application/rdf+xml     | alternate |

  Scenario Outline: No profile or media type returns default profile and media type with Link header
    When I send a GET request for object ID ""
    Then the response status should be 200
    And the response Content-Type should match "application/json"
    And the response link header should contain "profile token" "<expectedTokens>"

    Examples:

      | expectedTokens         |
      | la,la-framed,oai_dc,edm|

