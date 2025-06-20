package com.rijksmuseum.utils;

import io.restassured.response.Response;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiResponseValidator {

    private static final Logger logger = LoggerFactory.getLogger(ApiResponseValidator.class);

    public static void validateErrorMessageContains(Response response, String expectedText) {
        String contentType = response.getContentType();
        logger.info("Received Content-Type: {}", contentType);

        if (contentType != null && contentType.contains("application/json")) {
            String errorMsg = response.jsonPath().getString("detail");
            assertThat("Error message mismatch",
                    errorMsg != null ? errorMsg.toLowerCase() : "",
                    containsString(expectedText.toLowerCase()));
        } else {
            String responseBody = response.getBody().asString();
            assertThat("Expected error message to contain expected text",
                    responseBody != null ? responseBody.toLowerCase() : "",
                    containsString(expectedText.toLowerCase()));
        }
    }

    public static void verifyResponseStatusCode(Response response, int expectedStatusCode) {
        assertThat("Unexpected status code!", response.statusCode(), is(expectedStatusCode));
    }
}
