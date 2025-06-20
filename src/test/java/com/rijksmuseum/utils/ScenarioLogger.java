package com.rijksmuseum.utils;

import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ScenarioLogger {

    private static final Logger logger = LoggerFactory.getLogger(ScenarioLogger.class);

    public static void logScenarioStart(Scenario scenario) {
        logger.info("🎬 Starting Scenario: '{}' ...", scenario.getName());
        logTags(scenario);
    }

    public static void logScenarioEnd(Scenario scenario) {
        logger.info("✅ Finished Scenario: '{}' with status: {}",
                scenario.getName(), scenario.getStatus());
    }

    private static void logTags(Scenario scenario) {
        if (!scenario.getSourceTagNames().isEmpty()) {
            logger.info("🏷 Scenario Tags: {}", scenario.getSourceTagNames());
        }
    }

}
