package ru.cqa.server.scenario;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.cqa.scenario.creation.ScenarioCreationService;
import ru.cqa.scenario.definition.validation.ScenarioValidator;
import ru.cqa.scenario.extract.ExtractEngine;
import ru.cqa.scenario.extract.JsonPathExtractor;
import ru.cqa.scenario.repository.ScenarioRepository;
import ru.cqa.scenario.runtime.assertion.AssertionEngine;
import ru.cqa.scenario.runtime.assertion.EqualsAssertionEvaluator;
import ru.cqa.scenario.runtime.assertion.NotNullAssertionEvaluator;
import ru.cqa.scenario.runtime.execution.DefaultScenarioEngine;
import ru.cqa.scenario.runtime.execution.DefaultMoveRunner;
import ru.cqa.scenario.runtime.execution.DefaultStepRunner;
import ru.cqa.scenario.runtime.execution.ExecutionStrategy;
import ru.cqa.scenario.runtime.execution.MoveRunner;
import ru.cqa.scenario.runtime.execution.ScenarioEngine;
import ru.cqa.scenario.runtime.execution.SequentialExecutionStrategy;
import ru.cqa.scenario.runtime.execution.StepRunner;
import ru.cqa.scenario.runtime.executor.DefaultMoveExecutorRegistry;
import ru.cqa.scenario.runtime.executor.MoveExecutorRegistry;
import ru.cqa.scenario.serialization.ScenarioParser;

@Configuration
public class ScenarioEngineConfiguration {
    @Bean
    ScenarioValidator scenarioValidator(jakarta.validation.Validator validator) {
        return new ScenarioValidator(validator);
    }

    @Bean
    ScenarioCreationService scenarioCreationService(
            ScenarioParser parser,
            ScenarioValidator validator,
            ScenarioRepository repository) {
        return new ScenarioCreationService(parser, validator, repository);
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
            ScenarioValidator validator,
            ExecutionStrategy executionStrategy) {
        return new DefaultScenarioEngine(repository, validator, executionStrategy);
    }
}
