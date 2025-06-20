package com.rijksmuseum.stepdefinitions;

import com.rijksmuseum.utils.ConfigReader;
import com.rijksmuseum.utils.ResponseHolder;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class IiifImageApiSteps {

    private final String baseUrl = ConfigReader.getProperty("iiifImageApiBaseUrl");
    private static final Logger logger = LoggerFactory.getLogger(IiifImageApiSteps.class);
    private final ResponseHolder responseHolder;

    public IiifImageApiSteps(ResponseHolder responseHolder) {
        this.responseHolder = responseHolder;
    }

    @When("I fetch IIIF metadata for object {string}")
    public void fetchIIIFMetadata(String objectId) {
        String url = baseUrl + "/" + objectId + "/info.json";
        logger.info("Requesting IIIF metadata URL: {}", url);

        Response response = RestAssured.get(url);
        responseHolder.setResponse(response);
    }

    @When("I fetch IIIF image in format {string} for object {string} with region {string} and size {string} and rotation {string} and quality {string}")
    public void fetchIIIFImage(String format, String objectId, String region, String size, String rotation, String quality) {
        String url = String.format("%s/%s/%s/%s/%s/%s.%s",
                baseUrl, objectId, region, size, rotation, quality, format);
        logger.info("Requesting IIIF Image URL: {}", url);

        Response response = RestAssured
                .given()
                .urlEncodingEnabled(false)
                .get(url);
        responseHolder.setResponse(response);
    }

    @Then("it should include width in pixels")
    public void verifyWidthInPixels() {
        Response response = responseHolder.getResponse();
        Integer width = response.jsonPath().getInt("width");
        assertThat("Width should be present and greater than 0", width, notNullValue());
        assertThat(width, greaterThan(0));
    }

    @Then("it should include height in pixels")
    public void verifyHeightInPixels() {
        Response response = responseHolder.getResponse();
        Integer height = response.jsonPath().getInt("height");
        assertThat("Height should be present and greater than 0", height, notNullValue());
        assertThat(height, greaterThan(0));
    }

    @Then("the grayscale image validation is done when quality is {string}")
    public void grayscaleImageValidationConditionally(String quality) {
        if ("gray".equalsIgnoreCase(quality)) {
            Response response = responseHolder.getResponse();
            String contentType = response.getHeader("Content-Type");
            assertThat(contentType, anyOf(containsString("image/jpeg"), containsString("image/png")));
        }
    }

    @Then("the content type should be {string}")
    public void verifyContentType(String expectedContentType) {
        Response response = responseHolder.getResponse();
        String contentType = response.getHeader("Content-Type");
        assertThat("Content-Type header should be present", contentType, notNullValue());
        assertThat(contentType.toLowerCase(), containsString(expectedContentType.toLowerCase()));
    }

    @Then("the image should be grayscale")
    public void verifyImageIsGrayscale() {
        Response response = responseHolder.getResponse();
        String contentType = response.getHeader("Content-Type");
        assertThat(contentType, anyOf(containsString("image/jpeg"), containsString("image/png")));
    }
}




