package com.rijksmuseum.stepdefinitions;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.rijksmuseum.utils.ConfigReader;
import io.cucumber.datatable.DataTable;
import io.restassured.specification.RequestSpecification;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rijksmuseum.utils.ResponseHolder;

public class SearchAPISteps {

    private static final Logger logger = LoggerFactory.getLogger(SearchAPISteps.class);
    private final ResponseHolder responseHolder;
    private List<Map<String, String>> searchParameters;

    public SearchAPISteps(ResponseHolder responseHolder) {
        this.responseHolder = responseHolder;
    }

    @Given("the search API base URL is set")
    public void setSearchApiBaseUrl() {
        RestAssured.baseURI = ConfigReader.getProperty("baseUrl") + "/search/";
        logger.info("Requesting search API URL: {}", RestAssured.baseURI);
    }

    @When("I search artworks with {string} and {string}")
    public void searchArtworksWithParam(String parameter, String value) {
        Response response = RestAssured
                .given()
                .queryParam(parameter, value)
                .get("collection");

        logger.info("Calling URL: {}collection?{}={}", RestAssured.baseURI, parameter, value);
        responseHolder.setResponse(response);
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
        Response response = request.get("collection");
        responseHolder.setResponse(response);
    }

    @When("I search artworks with imageAvailable {string}")
    public void searchArtworksWithImageAvailable(String imageAvailable) {
        Response response = RestAssured.given()
                .queryParam("imageAvailable", imageAvailable)
                .get("collection");

        logger.info("Calling URL: {}collection?imageAvailable={}", RestAssured.baseURI, imageAvailable);
        responseHolder.setResponse(response);
    }

    @When("I request the next page using the pageToken from response")
    public void requestNextPageUsingPageToken() {
        Response currentResponse = responseHolder.getResponse();

        String nextUrl = currentResponse.jsonPath().getString("next.id");
        if (nextUrl == null || !nextUrl.contains("pageToken=")) {
            throw new RuntimeException("No valid next pageToken found in the response");
        }

        String pageToken = nextUrl.split("pageToken=")[1];
        Response response = RestAssured.given()
                .queryParam("pageToken", pageToken)
                .get("collection");

        logger.info("Calling URL: {}", nextUrl);
        responseHolder.setResponse(response);
    }

    @And("each response ID should contain parameters and values")
    public void validateSearchUrlContainsStoredParametersAndValues() {
        Response currentResponse = responseHolder.getResponse();
        String id = currentResponse.jsonPath().getString("partOf.id").toLowerCase()
                .replace("%2a", "*")
                .replace("%3f", "?");

        for (Map<String, String> row : searchParameters) {
            String parameter = row.get("parameter").toLowerCase();
            String value = row.get("value").toLowerCase();

            String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);
            String normalizedEncodedValue = encodedValue.toLowerCase().replace("%3f", "?");

            assertThat(String.format("Search URL '%s' missing parameter '%s'", id, parameter), id.contains(parameter));
            assertThat(String.format("Search URL '%s' missing value '%s' for parameter '%s'", id, normalizedEncodedValue, parameter)
                    , id.contains(normalizedEncodedValue));
        }
    }


    @Then("each art object should have an id")
    public void validateId() {
        Response currentResponse = responseHolder.getResponse();
        currentResponse.then().body("orderedItems.id", everyItem(notNullValue()));
    }


    @And("the resolved objectNumbers should match pattern {string}")
    public void verifyObjectNumbersMatchPatternStrict(String pattern) {
        Response currentResponse = responseHolder.getResponse();

        String regex = pattern.replace("*", ".*");
        logger.info("🔍 Verifying object numbers matching pattern '{}'", pattern);

        List<String> itemIds = currentResponse.jsonPath().getList("orderedItems.id");

        if (itemIds == null || itemIds.isEmpty()) {
            logger.warn("No 'orderedItems' found in the response." +
                    "Skipping objectNumber pattern verification.");
            return;
        }

        for (String itemId : itemIds) {
            logger.info("Processing item ID: {}", itemId);

            Response itemResponse = RestAssured.get(itemId);
            assertThat("Failed to fetch item details for ID: " + itemId +
                            ". Status code: " + itemResponse.statusCode(),
                            itemResponse.statusCode(), is(200));

            List<Map<String, Object>> identifiedBy = itemResponse.jsonPath().getList("identified_by");
            assertThat("No 'identified_by' found for item ID: " + itemId,
                    identifiedBy != null && !identifiedBy.isEmpty());

            boolean matchFoundForThisItem = false;

            for (Map<String, Object> identifierBlock : identifiedBy) {
                String content = (String) identifierBlock.get("content");
                assertThat("Found identifier block with null content for item ID: " +
                        itemId, content, notNullValue());

                if (content.matches(regex)) {
                    logger.info("Found content matching pattern: {} for item ID: {}", content, itemId);
                    matchFoundForThisItem = true;

                    @SuppressWarnings("unchecked")
                    List<Map<String, String>> classifiedAsList =
                            (List<Map<String, String>>) identifierBlock.get("classified_as");

                    assertThat("No 'classified_as' found for identifier with content: '" + content +
                                    "' in item ID: " + itemId,
                            classifiedAsList != null && !classifiedAsList.isEmpty());

                    boolean objectNumberFound = false;

                    for (Map<String, String> classifier : classifiedAsList) {
                        String classifierId = classifier.get("id");
                        if (classifierId == null || !classifierId.startsWith("https://id")) continue;

                        logger.info("Received classified_as ID: {}", classifierId);
                        Response classifierResponse = RestAssured.get(classifierId);

                        assertThat("Failed to receive classified_as ID: " + classifierId + " with status: " +
                                        classifierResponse.statusCode(),
                                classifierResponse.statusCode(), is(200));

                        List<Map<String, Object>> labels = classifierResponse.jsonPath().getList("identified_by");
                        assertThat("No 'identified_by' labels found for classified_as ID: " + classifierId,
                                labels != null && !labels.isEmpty());

                        for (Map<String, Object> label : labels) {
                            String labelContent = (String) label.get("content");
                            if ("object number".equalsIgnoreCase(labelContent)) {
                                objectNumberFound = true;
                                break;
                            }
                        } if (objectNumberFound) break;

                    } assertThat("For content '" + content + "' in item " + itemId +
                              ", expected a classified_as with 'object number' content but none found.",
                              objectNumberFound);
                }
            } assertThat("No 'identified_by' block with content matching pattern '" + pattern +
                    "' found for item ID: " + itemId, matchFoundForThisItem);
        }
    }
}

