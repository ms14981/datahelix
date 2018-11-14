package com.scottlogic.deg.generator.cucumber.steps;

import com.scottlogic.deg.generator.cucumber.utils.*;
import com.scottlogic.deg.generator.generation.GenerationConfig;
import com.scottlogic.deg.generator.inputs.InvalidProfileException;
import cucumber.api.java.Before;
import cucumber.api.java.en.*;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;

public class GeneralTestStep {

    private DegTestState state;
    private DegTestHelper testHelper;

    public GeneralTestStep(DegTestState state){
        this.state = state;
    }

    @Before
    public void BeforeEach() {
        this.state.clearState();
        this.testHelper = new DegTestHelper(state);
    }

    @Given("there is a field (.+)$")
    public void thereIsAField(String fieldName) {
        this.state.addField(fieldName);
    }

    @Given("^the following fields exist:$")
    public void thereAreFields(List<String> fields) {
        fields.forEach(this::thereIsAField);
    }

    @When("the generation strategy is {generationStrategy}")
    public void setTheGenerationStrategy(GenerationConfig.DataGenerationType strategy) {
        this.state.generationStrategy = strategy;
    }

    @And("^(.+) is null$")
    public void fieldIsNull(String fieldName) throws Exception{
        this.state.addConstraint(fieldName, "null", null);
    }

    @And("^(.+) is anything but null$")
    public void fieldIsNotNull(String fieldName) throws Exception{
        this.state.addNotConstraint(fieldName, "null", null);
    }

    @But("the profile is invalid as (.+) can't be ([a-z ]+) (((\".*\")|([0-9]+(.[0-9]+){1}))+)")
    public void fieldIsInvalid(String fieldName, String constraint, String value) {
        Object parsedValue;
        if (value.startsWith("\"") && value.endsWith("\"")) {
            parsedValue = value.substring(1, value.length() - 1);
        }  else if (value.contains(".")){
            parsedValue = Double.parseDouble(value);
        } else {
            parsedValue = Integer.parseInt(value);
        }

        this.state.addConstraint(fieldName, constraint, parsedValue);
        Assert.assertThat(
            "Expected invalid profile",
            this.testHelper.getThrownExceptions(),
            hasItem(isA(InvalidProfileException.class)));
    }

    @Then("^I am presented with an error message$")
    public void dataGeneratorShouldError() {
        testHelper.generateAndGetData();
        Assert.assertThat(testHelper.generatorHasThrownException(), is(true));
    }

    @And("^no data is created$")
    public void noDataIsCreated() {
        testHelper.generateAndGetData();
        Assert.assertFalse(testHelper.hasDataBeenGenerated());
    }

    @Then("^the following data should be generated:$")
    public void theFollowingDataShouldBeGenerated(List<Map<String, String>> expectedResultsTable) {
        GeneratedTestData data = getExpectedAndGeneratedData(expectedResultsTable);

        Assert.assertThat(
            "Exceptions thrown during generation",
            testHelper.getThrownExceptions(),
            empty());
        Assert.assertThat(data.generatedData, new RowsMatchAnyOrderMatcher(data.expectedData));
    }

    @Then("^the following data should be generated in order:$")
    public void theFollowingDataShouldBeGeneratedInOrder(List<Map<String, String>> expectedResultsTable) {
        GeneratedTestData data = getExpectedAndGeneratedData(expectedResultsTable);

        Assert.assertThat(
            "Exceptions thrown during generation",
            testHelper.getThrownExceptions(),
            empty());
        Assert.assertThat(data.generatedData, contains(data.expectedData));
    }

    @Then("^the following data should be included in what is generated:$")
    public void theFollowingDataShouldBeContainedInActual(List<Map<String, String>> expectedResultsTable) {
        GeneratedTestData data = getExpectedAndGeneratedData(expectedResultsTable);

        Assert.assertThat(
            "Exceptions thrown during generation",
            testHelper.getThrownExceptions(),
            empty());
        Assert.assertThat(data.generatedData, new RowsPresentMatcher(data.expectedData));
    }

    @Then("^the following data should not be included in what is generated:$")
    public void theFollowingDataShouldNotBeContainedInActual(List<Map<String, String>> expectedResultsTable) {
        GeneratedTestData data = getExpectedAndGeneratedData(expectedResultsTable);

        Assert.assertThat(
            "Exceptions thrown during generation",
            testHelper.getThrownExceptions(),
            empty());
        Assert.assertThat(data.generatedData, new RowsAbsentMatcher(data.expectedData));
    }

    private List <List<Object>> getComparableExpectedResults(List<Map<String, String>> expectedResultsTable){
        return expectedResultsTable
            .stream()
            .map(row -> new ArrayList<>(row.values()))
            .map(row -> row.stream().map(GeneratorTestUtilities::parseInput).collect(Collectors.toList()))
            .collect(Collectors.toList());
    }

    private GeneratedTestData getExpectedAndGeneratedData(List<Map<String, String>> expectedResultsTable){
        List <List<Object>> expectedRowsOfResults = getComparableExpectedResults(expectedResultsTable);
        List <List<Object>> data = testHelper.generateAndGetData();
        return new GeneratedTestData(expectedRowsOfResults, data);
    }

    class GeneratedTestData {
        List <List<Object>> expectedData;
        List <List<Object>> generatedData;

        GeneratedTestData(List <List<Object>> expectedData, List <List<Object>> generatedData){
            this.expectedData = expectedData;
            this.generatedData = generatedData;
        }
    }
}
