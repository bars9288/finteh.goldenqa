package ru.cqa.cases.validation.physical;

import java.util.List;

import ru.cqa.dto.PartyDTO;

@FunctionalInterface
public interface PhysicalRule {

    List<String> checkPhysical(PartyDTO requestParty);
}
