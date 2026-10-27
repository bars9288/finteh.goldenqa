package ru.cqa.business.validation.rules.party.legal;

import ru.cqa.business.validation.rules.Rule;
import ru.cqa.dto.PartyDTO;

import java.util.List;

public final class NameRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        return List.of();
    }
}
