package ru.cqa.cases.validation.rules.party.list.legal;

import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.dto.PartyDTO;

import java.util.List;

public final class NameRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        return List.of();
    }
}
