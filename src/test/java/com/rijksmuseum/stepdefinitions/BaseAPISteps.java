package com.rijksmuseum.stepdefinitions;

import com.rijksmuseum.utils.ScenarioContext;
import io.restassured.response.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;

public abstract class BaseAPISteps {

    protected final ScenarioContext scenarioContext;

    // Constructor injection of shared context
    public BaseAPISteps(ScenarioContext scenarioContext) {
        this.scenarioContext = scenarioContext;
    }

    // Reusable method: verify HTTP status code
    protected void verifyResponseStatus(int expectedStatus) {
        Response response = scenarioContext.getResponse();
        if (response == null) {
            throw new IllegalStateException("Response not set in ScenarioContext.");
        }
        assertEquals("Unexpected response status", expectedStatus, response.statusCode());
    }

    // Reusable method: verify error message contains expected text
    protected void verifyErrorMessageContains(String expectedText) {
        Response response = scenarioContext.getResponse();
        if (response == null) {
            throw new IllegalStateException("Response not set in ScenarioContext.");
        }

        String contentType = response.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            String errorMsg = response.jsonPath().getString("detail");
            if (errorMsg == null) {
                throw new AssertionError("Response JSON does not contain 'detail' field");
            }
            assertThat("Error message mismatch", errorMsg.toLowerCase(), containsString(expectedText.toLowerCase()));
        } else {
            String responseBody = response.getBody().asString();
            assertThat("Error message mismatch", responseBody.toLowerCase(), containsString(expectedText.toLowerCase()));
        }
    }
}
