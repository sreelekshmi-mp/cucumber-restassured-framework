package com.rijksmuseum.utils;

import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScenarioLogger {

    private static final Logger logger = LoggerFactory.getLogger(ScenarioLogger.class);

    public static void logScenarioStart(Scenario scenario) {
        String featureName = extractFeatureFileName(scenario.getId());
        logger.info("🎬 Starting Scenario: '{}' from Feature: '{}'", scenario.getName(), featureName);
        logTags(scenario);
    }

    public static void logScenarioEnd(Scenario scenario) {
        String featureName = extractFeatureFileName(scenario.getId());
        logger.info("✅ Finished Scenario: '{}' from Feature: '{}' with status: {}",
                scenario.getName(), featureName, scenario.getStatus());
    }

    private static void logTags(Scenario scenario) {
        if (!scenario.getSourceTagNames().isEmpty()) {
            logger.info("🏷 Scenario Tags: {}", scenario.getSourceTagNames());
        }
    }

    private static String extractFeatureFileName(String scenarioId) {
        Pattern pattern = Pattern.compile(".*/([^/]+\\.feature):\\d+");
        Matcher matcher = pattern.matcher(scenarioId);
        return matcher.find() ? matcher.group(1) : "Unknown Feature File";
    }
}
