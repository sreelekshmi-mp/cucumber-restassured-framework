package com.rijksmuseum.steps;

import com.rijksmuseum.utils.ApiResponseValidator;
import com.rijksmuseum.utils.ResponseHolder;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;

public class CommonStepDefinitions {


    private final ResponseHolder responseHolder;

    public CommonStepDefinitions(ResponseHolder responseHolder) {
        this.responseHolder = responseHolder;
    }

    @Then("the error message should contain {string}")
    public void validateErrorMessageContainsStep(String expectedText) {
        Response response = responseHolder.getResponse();
        ApiResponseValidator.validateErrorMessageContains(response, expectedText);
    }

    @Then("the response status should be {int}")
    public void verifyResponseStatusCodeStep(int statusCode) {
        Response response = responseHolder.getResponse();
        ApiResponseValidator.verifyResponseStatusCode(response, statusCode);
    }
}
