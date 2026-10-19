package io.mb.qa.runners;

import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Single entry point: discovers every .feature under src/test/resources/features.
 * Filter with:  mvn test -Dcucumber.filter.tags="@smoke"
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "io.mb.qa.stepdefinitions,io.mb.qa.hooks")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@smoke")
public class RunCucumberTest {
}
