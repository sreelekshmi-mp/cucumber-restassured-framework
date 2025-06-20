package com.rijksmuseum.hooks;

import com.rijksmuseum.utils.ScenarioLogger;
import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;

public class Hooks {

    @Before
    public void beforeScenario(Scenario scenario) {
        ScenarioLogger.logScenarioStart(scenario);
    }

    @After
    public void afterScenario(Scenario scenario) {
        ScenarioLogger.logScenarioEnd(scenario);
    }
}
