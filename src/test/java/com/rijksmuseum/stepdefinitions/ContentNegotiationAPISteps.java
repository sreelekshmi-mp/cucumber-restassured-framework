package com.rijksmuseum.stepdefinitions;

import com.rijksmuseum.utils.ConfigReader;
import com.rijksmuseum.utils.ResponseHolder;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentNegotiationAPISteps {

    private static final Logger logger = LoggerFactory.getLogger(ContentNegotiationAPISteps.class);
    private final ResponseHolder responseHolder;

    public ContentNegotiationAPISteps(ResponseHolder responseHolder) {
        this.responseHolder = responseHolder;
    }

    @Given("the Content negotiation API base URL is set")
    public void setBaseUrl() {
        RestAssured.baseURI = ConfigReader.getProperty("baseUrl");
        logger.info("Requesting Content negotiation API URL: {}", RestAssured.baseURI);
    }


    @When("I send a GET request for object ID {string}")
    public void sendGetRequestForObjectId(String objectId) {

        Response response;
        if (objectId == null || objectId.isEmpty()) {
            response = RestAssured
                    .given()
                    .get();
        } else {
            response = RestAssured
                    .given()
                    .get("/" + objectId);
        }
        responseHolder.setResponse(response);
    }


    @And("the response link header should contain {string} {string}")
    public void verifyLinkHeaderValue(String checkType, String expectedValue) {

        Response response = responseHolder.getResponse();

        String linkHeader = response.header("Link");
        assertThat("Link header should be present", linkHeader, notNullValue());

        String normalizedCheckType = checkType.toLowerCase();

        switch (normalizedCheckType) {
            case "profile token":
                for (String token : expectedValue.split(",")) {
                    assertThat("Link header should contain profile token: " + token.trim(),
                            linkHeader, containsString("token=\"" + token.trim() + "\""));}
                break;

            case "media type":
                assertThat("Link header should contain media type: " + expectedValue,
                        linkHeader, containsString("type=\"" + expectedValue + "\""));
                break;

            case "relation":
                assertThat("Link header should contain relation: " + expectedValue,
                        linkHeader, containsString("rel=\"" + expectedValue + "\""));
                break;

            case "anchor":
                assertThat("Link header should contain anchor: " + expectedValue,
                        linkHeader, containsString("anchor=<" + expectedValue + ">"));
                break;
            default:
                throw new IllegalArgumentException("Unsupported check type: " + checkType);
        }
    }


    @When("I send a GET request for object ID {string} with query parameter _profile={string}" +
            " and optional media type {string}")
    public void sendGetRequestWithObjectIdAndProfile(String objectId, String profile, String mediaType) {

        logger.info("Sending GET request to /{} with _profile={}", objectId, profile);

        RequestSpecification request = RestAssured.given()
                .queryParam("?_profile", profile);

        if (mediaType != null && !mediaType.isEmpty()) {
            request = request.queryParam("_mediatype", mediaType);
        }
        Response response = request.get(objectId);
        responseHolder.setResponse(response);


    }

    @Then("the response Content-Type should match {string}")
    public void verifyContentType(String expectedContentType) {

        Response response = responseHolder.getResponse();

        String actualContentType = response.header("Content-Type");
        logger.info("Actual Content-Type: {}", actualContentType);
        assertThat("Content-Type should contain expected value",
                actualContentType.toLowerCase(), containsString(expectedContentType.toLowerCase()));
    }
}
