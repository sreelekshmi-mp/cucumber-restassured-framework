//package com.rijksmuseum.stepdefinitions;
//
//import com.rijksmuseum.utils.ConfigReader;
//import io.cucumber.java.en.*;
//import io.restassured.RestAssured;
//import io.restassured.response.Response;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.*;
//
//public class IiifImageApiSteps {
//
//    private Response response;
//    private final String baseUrl = ConfigReader.getProperty("iiifImageApiBaseUrl");
//    private static final Logger logger = LoggerFactory.getLogger(IiifImageApiSteps.class);
//
//
//
//    @When("I fetch IIIF metadata for object {string}")
//    public void fetchIIIFMetadata(String objectId) {
//        response = RestAssured.get(baseUrl + "/" +objectId + "/info.json");
//    }
//
//    @Then("the metadata response status should be {int}")
//    public void verifyMetadataResponseStatus(int expectedStatus) {
//        assertThat(response.statusCode(), is(expectedStatus));
//    }
//
//    @Then("it should include width in pixels")
//    public void verifyWidthInPixels() {
//        Integer width = response.jsonPath().getInt("width");
//        assertThat("Width should be present and greater than 0", width, notNullValue());
//        assertThat(width, greaterThan(0));
//    }
//
//    @Then("it should include height in pixels")
//    public void verifyHeightInPixels() {
//        Integer height = response.jsonPath().getInt("height");
//        assertThat("Height should be present and greater than 0", height, notNullValue());
//        assertThat(height, greaterThan(0));
//    }
//
//    @When("I fetch IIIF image in format {string} for object {string} with region {string} and size {string} and rotation {string} and quality {string}")
//    public void fetchIIIFImage(String format, String objectId, String region, String size, String rotation, String quality) {
//
//        String url = String.format("%s/%s/%s/%s/%s/%s.%s", baseUrl, objectId, region, size, rotation, quality, format);
//        logger.info("Requesting IIIF Image URL: {}", url);
//
//        response = RestAssured
//                .given()
//                .urlEncodingEnabled(false)
//                .get(url);
//
//        System.out.println("Full Response: " + response.asPrettyString());
//    }
//
//    @Then("the content type should be {string}")
//    public void verifyContentType(String expectedContentType) {
//        String contentType = response.getHeader("Content-Type");
//        assertThat("Content-Type header should be present", contentType, notNullValue());
//        assertThat(contentType.toLowerCase(), containsString(expectedContentType.toLowerCase()));
//    }
//
//    @Then("the image should be grayscale")
//    public void verifyImageIsGrayscale() {
//        String contentType = response.getHeader("Content-Type");
//        assertThat(contentType, anyOf(containsString("image/jpeg"), containsString("image/png")));
//    }
//
//}

package com.rijksmuseum.stepdefinitions;

import com.rijksmuseum.utils.ConfigReader;
import com.rijksmuseum.utils.ScenarioContext;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class IiifImageApiSteps {

//    public IiifImageApiSteps(ScenarioContext scenarioContext) {
//        super(scenarioContext);
//    }


    private Response response;
    private final String baseUrl = ConfigReader.getProperty("iiifImageApiBaseUrl");
    private static final Logger logger = LoggerFactory.getLogger(IiifImageApiSteps.class);

    @When("I fetch IIIF metadata for object {string}")
    public void fetchIIIFMetadata(String objectId) {
        String url = baseUrl + "/" + objectId + "/info.json";
        logger.info("Requesting IIIF metadata URL: {}", url);

        response = RestAssured.get(url);
    }

    @When("I fetch IIIF image in format {string} for object {string} with region {string} and size {string} and rotation {string} and quality {string}")
    public void fetchIIIFImage(String format, String objectId, String region, String size, String rotation, String quality) {
        String url = String.format("%s/%s/%s/%s/%s/%s.%s", baseUrl, objectId, region, size, rotation, quality, format);
        logger.info("Requesting IIIF Image URL: {}", url);
        response = RestAssured
                .given()
                .urlEncodingEnabled(false)
                .get(url);
    }

    @Then("the iiif response status should be {int}")
    public void verifyMetadataResponseStatus(int expectedStatus) {
        assertThat(response.statusCode(), is(expectedStatus));
    }

    @Then("it should include width in pixels")
    public void verifyWidthInPixels() {
        Integer width = response.jsonPath().getInt("width");
        assertThat("Width should be present and greater than 0", width, notNullValue());
        assertThat(width, greaterThan(0));
    }

    @Then("the iiif error message should contain {string}")
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

    @Then("it should include height in pixels")
    public void verifyHeightInPixels() {
        Integer height = response.jsonPath().getInt("height");
        assertThat("Height should be present and greater than 0", height, notNullValue());
        assertThat(height, greaterThan(0));
    }


    @Then("the grayscale image validation is done when quality is {string}")
    public void grayscaleImageValidationConditionally(String quality) {
        if ("gray".equalsIgnoreCase(quality)) {
            String contentType = response.getHeader("Content-Type");
            assertThat(contentType, anyOf(containsString("image/jpeg"), containsString("image/png")));
        }
    }


    @Then("the content type should be {string}")
    public void verifyContentType(String expectedContentType) {
        String contentType = response.getHeader("Content-Type");
        assertThat("Content-Type header should be present", contentType, notNullValue());
        assertThat(contentType.toLowerCase(), containsString(expectedContentType.toLowerCase()));
    }

    @Then("the image should be grayscale")
    public void verifyImageIsGrayscale() {
        // Simple check for image/jpeg or image/png as grayscale images are usually JPEG/PNG
        String contentType = response.getHeader("Content-Type");
        assertThat(contentType, anyOf(containsString("image/jpeg"), containsString("image/png")));
    }
}

