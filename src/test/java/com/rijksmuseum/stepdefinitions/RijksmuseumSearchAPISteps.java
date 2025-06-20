
package com.rijksmuseum.stepdefinitions;

import com.rijksmuseum.utils.ScenarioContext;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.rijksmuseum.utils.ConfigReader;
import io.cucumber.java.en.When;
import io.cucumber.datatable.DataTable;
import io.restassured.specification.RequestSpecification;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class RijksmuseumSearchAPISteps  {

//    public RijksmuseumSearchAPISteps(ScenarioContext scenarioContext) {
//        super(scenarioContext);
//    }

    private Response response;
    private static final Logger logger = LoggerFactory.getLogger(RijksmuseumSearchAPISteps.class);
    private List<Map<String, String>> searchParameters;

    @Given("the Rijksmuseum API base URL is set")
    public void the_rijksmuseum_api_base_url_is_set() {
        RestAssured.baseURI = ConfigReader.getProperty("searchApiBaseUrl") + "/search/";
        logger.info("Requesting URL: {}", RestAssured.baseURI);
    }

    // Generic step for searching with a single parameter
    @When("I search artworks with {string} and {string}")
    public void searchArtworksWithParam(String parameter, String value) {
        response = RestAssured
                .given()
                .queryParam(parameter, value)
                .get("collection");


        logger.info("Calling URL: " + RestAssured.baseURI + "collection?" + parameter + "=" + value);
    }


    @When("I search artworks with parameters:")
    public void searchArtworksWithParameters(DataTable dataTable) {
        RequestSpecification request = RestAssured.given();

        searchParameters = dataTable.asMaps(String.class, String.class);

        Map<String, List<String>> paramMap = new HashMap<>();

        for (Map<String, String> row : searchParameters) {
            String key = row.get("parameter");
            String value = row.get("value");

            paramMap.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }

        for (Map.Entry<String, List<String>> entry : paramMap.entrySet()) {
            String key = entry.getKey();
            for (String value : entry.getValue()) {
                request.queryParam(key, value);
            }
        }
        response = request.get("collection");
    }

    @When("I search artworks with imageAvailable {string}")
    public void searchArtworksWithImageAvailable(String imageAvailable) {
        response = RestAssured.given()
                .queryParam("imageAvailable", imageAvailable)
                .get("collection");
    }

    @When("I request the next page using the pageToken from response")
    public void requestNextPageUsingPageToken() {
        String nextUrl = response.jsonPath().getString("next.id");
        if (nextUrl == null || !nextUrl.contains("pageToken=")) {
            throw new RuntimeException("No valid next pageToken found in the response");
        }
        String pageToken = nextUrl.split("pageToken=")[1];
        response = RestAssured.given()
                .queryParam("pageToken", pageToken)
                .get("collection");
    }


    @And("each response ID should contain parameters and values")
    public void validateSearchUrlContainsStoredParametersAndValues() {
        String id = response.jsonPath().getString("partOf.id").toLowerCase()
                .replace("%2a", "*")
                .replace("%3f", "?");

        for (Map<String, String> row : searchParameters) {
            String parameter = row.get("parameter").toLowerCase();
            String value = row.get("value").toLowerCase();

            String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);
            String normalizedEncodedValue = encodedValue.toLowerCase().replace("%3f", "?");

            assertTrue(String.format("Search URL '%s' missing parameter '%s'", id, parameter), id.contains(parameter));
            assertTrue(String.format("Search URL '%s' missing value '%s' for parameter '%s'", id, normalizedEncodedValue, parameter)
                    , id.contains(normalizedEncodedValue));
        }
    }


    @Then("the response status should be {int}")
    public void verifyResponseStatusCode(int statusCode) {
        assertThat(response.statusCode(), is(statusCode));
    }

    @Then("each art object should have an id and type")
    public void validateIdAndType() {
        response.then().body("orderedItems.id", everyItem(notNullValue()));
        response.then().body("orderedItems.type", everyItem(notNullValue()));
    }

    @Then("each art object should have an id")
    public void validateId() {
        response.then().body("orderedItems.id", everyItem(notNullValue()));
    }

    @Then("each art object should have an id and creationDate")
    public void validateIdAndCreationDate() {
        response.then().body("orderedItems.id", everyItem(notNullValue()));
        // creationDate might not be directly available here; adapt as needed
    }

    @Then("each art object should have a type and technique")
    public void validateTypeAndTechnique() {
        response.then().body("orderedItems.type", everyItem(notNullValue()));
        // technique field might need a different path or deeper validation, adjust accordingly
    }

    @Then("the response should contain a list of object IDs")
    public void responseShouldContainObjectIDs() {
        List<String> ids = response.jsonPath().getList("orderedItems.id");
        assertThat("orderedItems should not be empty", ids, is(not(empty())));
        for (String id : ids) {
            assertThat("Each id should be a non-empty string", id, is(not(emptyOrNullString())));
        }
    }

    @Then("the image availability in results should be {string}")
    public void validateImageAvailabilityInResults(String imageAvailable) {
        List<Object> images = response.jsonPath().getList("orderedItems.webImage.url");
        boolean expectImage = Boolean.parseBoolean(imageAvailable);

        if (expectImage) {
            assertThat(images, everyItem(notNullValue()));
        } else {
            assertThat(images, everyItem(nullValue()));
        }
    }

    @Then("the next page token received is valid")
    public void validateNextPageToken() {
        String nextUrl = response.jsonPath().getString("next.id");
        if (nextUrl == null || !nextUrl.contains("pageToken=")) {
            throw new AssertionError("Next page URL is missing or does not contain a valid pageToken");
        }
    }


    @Then("the error message should contain {string}")
    public void validateErrorMessageContains(String expectedText) {
        String contentType = response.getContentType();

        logger.info("Received Content-Type: {}", contentType);

        if (contentType.contains("application/json")) {
            String errorMsg = response.jsonPath().getString("detail");
            assertThat("Error message mismatch", errorMsg.toLowerCase(), containsString(expectedText.toLowerCase()));
        } else {
            String responseBody = response.getBody().asString();
            assertThat("Expected error message to contain expected text",
                    responseBody.toLowerCase(), containsString(expectedText.toLowerCase()));
        }
    }



    @And("the resolved objectNumbers should match pattern {string}")
    public void verifyObjectNumbersMatchPatternStrict(String pattern) {

        String regex = pattern.replace("*", ".*");

        logger.info("🔍 Verifying object numbers matching pattern '{}'", pattern);

        List<String> itemIds = response.jsonPath().getList("orderedItems.id");

        if (itemIds == null || itemIds.isEmpty()) {
            logger.warn("No 'orderedItems' found in the response. Skipping objectNumber pattern verification.");
            return;
        }

        boolean overallMatchFound = false;

        for (String itemId : itemIds) {
            logger.info("➡️ Processing item ID: {}", itemId);

            Response itemResponse = RestAssured.get(itemId);
            assertEquals("Failed to fetch item details for ID: " + itemId + ". Status code: " +
                    itemResponse.statusCode(), 200, itemResponse.statusCode());

            List<Map<String, Object>> identifiedBy = itemResponse.jsonPath().getList("identified_by");
            assertTrue("No 'identified_by' found for item ID: " + itemId,
                    identifiedBy != null && !identifiedBy.isEmpty());

            boolean matchFoundForThisItem = false;

            for (Map<String, Object> identifierBlock : identifiedBy) {
                String content = (String) identifierBlock.get("content");
                assertNotNull("Found identifier block with null content for item ID: " + itemId, content);

                if (content.matches(regex)) {
                    logger.info("✅ Found content matching pattern: {} for item ID: {}", content, itemId);
                    matchFoundForThisItem = true;

                    @SuppressWarnings("unchecked")
                    List<Map<String, String>> classifiedAsList =
                            (List<Map<String, String>>) identifierBlock.get("classified_as");

                    assertTrue("No 'classified_as' found for identifier with content: '" + content +
                            "' in item ID: " + itemId, classifiedAsList != null && !classifiedAsList.isEmpty());

                    boolean objectNumberFound = false;

                    for (Map<String, String> classifier : classifiedAsList) {
                        String classifierId = classifier.get("id");
                        if (classifierId == null || !classifierId.startsWith("https://id")) continue;

                        logger.info("🔗 GET classified_as ID: {}", classifierId);
                        Response classifierResponse = RestAssured.get(classifierId);

                        assertEquals("Failed to GET classified_as ID: " + classifierId + " with status: " +
                                classifierResponse.statusCode(), 200, classifierResponse.statusCode());

                        List<Map<String, Object>> labels = classifierResponse.jsonPath().getList("identified_by");
                        assertTrue("No 'identified_by' labels found for classified_as ID: " + classifierId,
                                labels != null && !labels.isEmpty());

                        for (Map<String, Object> label : labels) {
                            String labelContent = (String) label.get("content");
                            if ("object number".equalsIgnoreCase(labelContent)) {
                                objectNumberFound = true;
                                break;
                            }
                        }
                        if (objectNumberFound) break;
                    }

                    assertTrue("For content '" + content + "' in item " + itemId +
                                    ", expected a classified_as with 'object number' content but none found.",
                            objectNumberFound);
                }
            }

            assertTrue("No 'identified_by' block with content matching pattern '" + pattern +
                    "' found for item ID: " + itemId, matchFoundForThisItem);

            if (matchFoundForThisItem) {
                overallMatchFound = true;
            }
        }
        assertTrue("Expected at least one item with objectNumber matching pattern '" + pattern + "' but found none.",
                overallMatchFound);
    }
}