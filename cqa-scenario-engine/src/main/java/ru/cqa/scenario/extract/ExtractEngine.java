package ru.cqa.scenario.extract;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ru.cqa.scenario.model.Extract;
import ru.cqa.scenario.result.MoveResult;

/** Выбирает extract-оры по типу и формирует значения для контекста следующего шага. */
public final class ExtractEngine {
    private final Map<String, Extractor> extractors;

    public ExtractEngine(Collection<? extends Extractor> extractors) {
        Objects.requireNonNull(extractors, "extractors");
        Map<String, Extractor> byType = new LinkedHashMap<>();
        for (Extractor extractor : extractors) {
            Objects.requireNonNull(extractor, "extractor");
            if (byType.putIfAbsent(extractor.getType(), extractor) != null) {
                throw new IllegalArgumentException("Extractor уже зарегистрирован: " + extractor.getType());
            }
        }
        this.extractors = Map.copyOf(byType);
    }

    public Map<String, Object> extract(List<Extract> definitions, MoveResult result) {
        Objects.requireNonNull(definitions, "definitions");
        Objects.requireNonNull(result, "result");
        Map<String, Object> values = new LinkedHashMap<>();
        for (Extract definition : definitions) {
            Extractor extractor = extractors.get(definition.type());
            if (extractor == null) {
                throw new IllegalArgumentException("Неизвестный тип Extract: " + definition.type());
            }
            values.put(definition.targetVariable(), extractor.extract(definition, result));
        }
        return Collections.unmodifiableMap(values);
    }
}
