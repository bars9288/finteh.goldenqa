package ru.cqa.bootstrap.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.cqa.api.scenario.ScenarioCreator;
import ru.cqa.api.scenario.ScenarioEngine;
import ru.cqa.api.scenario.ScenarioParser;
import ru.cqa.api.scenario.ScenarioRepository;
import ru.cqa.api.validation.FormValidator;
import ru.cqa.api.validation.Validator;
import ru.cqa.model.scenario.Scenario;
import ru.cqa.scenario.creation.ScenarioCreationService;
import ru.cqa.scenario.definition.validation.ScenarioValidator;
import ru.cqa.scenario.extract.ExtractEngine;
import ru.cqa.scenario.extract.JsonPathExtractor;
import ru.cqa.scenario.runtime.assertion.AssertionEngine;
import ru.cqa.scenario.runtime.assertion.EqualsAssertionEvaluator;
import ru.cqa.scenario.runtime.assertion.NotNullAssertionEvaluator;
import ru.cqa.scenario.runtime.execution.DefaultMoveRunner;
import ru.cqa.scenario.runtime.execution.DefaultScenarioEngine;
import ru.cqa.scenario.runtime.execution.DefaultStepRunner;
import ru.cqa.scenario.runtime.execution.ExecutionStrategy;
import ru.cqa.scenario.runtime.execution.MoveRunner;
import ru.cqa.scenario.runtime.execution.SequentialExecutionStrategy;
import ru.cqa.scenario.runtime.execution.StepRunner;
import ru.cqa.scenario.runtime.executor.DefaultMoveExecutorRegistry;
import ru.cqa.scenario.runtime.executor.MoveExecutorRegistry;
import ru.cqa.server.scenario.HttpMoveExecutor;

@Configuration
public class ScenarioEngineConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioEngineConfiguration.class);

    @Bean
    Validator<Scenario> scenarioValidator(FormValidator formValidator) {
        return new ScenarioValidator(formValidator);
    }

    @Bean
    ScenarioCreator scenarioCreationService(
            ScenarioParser parser,
            Validator<Scenario> scenarioValidator,
            ScenarioRepository repository) {
        return new ScenarioCreationService(parser, scenarioValidator, repository);
    }

    @Bean
    MoveExecutorRegistry moveExecutorRegistry(HttpMoveExecutor httpMoveExecutor) {
        return new DefaultMoveExecutorRegistry(List.of(httpMoveExecutor));
    }

    @Bean
    MoveRunner moveRunner(MoveExecutorRegistry moveExecutorRegistry) {
        return new DefaultMoveRunner(moveExecutorRegistry);
    }

    @Bean
    AssertionEngine assertionEngine() {
        return new AssertionEngine(List.of(
                new EqualsAssertionEvaluator(),
                new NotNullAssertionEvaluator()));
    }

    @Bean
    ExtractEngine extractEngine() {
        return new ExtractEngine(List.of(new JsonPathExtractor()));
    }

    @Bean
    StepRunner stepRunner(MoveRunner moveRunner, AssertionEngine assertionEngine, ExtractEngine extractEngine) {
        return new DefaultStepRunner(moveRunner, assertionEngine, extractEngine);
    }

    @Bean
    ExecutionStrategy executionStrategy(StepRunner stepRunner) {
        return new SequentialExecutionStrategy(stepRunner);
    }

    @Bean
    ScenarioEngine scenarioEngine(
            ScenarioRepository repository,
            Validator<Scenario> scenarioValidator,
            ExecutionStrategy executionStrategy) {
        return new DefaultScenarioEngine(repository, scenarioValidator, executionStrategy);
    }
}
