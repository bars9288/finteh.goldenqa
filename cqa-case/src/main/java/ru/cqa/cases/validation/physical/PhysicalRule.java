package ru.cqa.cases.validation.physical;

import java.util.List;

import ru.cqa.dto.PartyDTO;
import ru.cqa.dto.SaveAndMergeDTO;

/**
 * Одно правило проверки запроса saveAndMerge — то, что аннотацией DTO не выразить.
 *
 * <p>Правила запускаются даже когда форма запроса нарушена (клиент получает все
 * ошибки за один заход), поэтому правило само переживает {@code null} в полях.
 */
@FunctionalInterface
public interface PhysicalRule {

    /**
     * Ошибки в формате «путь: сообщение», например {@code party[0].field: ...};
     * пустой список — правило соблюдено.
     */
    List<String> checkPhysical(PartyDTO requestParty);
}
