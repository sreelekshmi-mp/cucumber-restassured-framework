package com.rijksmuseum.utils;

import io.restassured.response.Response;

public class ScenarioContext {

    private Response response;

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    // You can add other shared data here if needed
}
