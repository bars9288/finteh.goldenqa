package ru.cqa.bootstrap;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import ru.cqa.api.validation.FormValidator;
import ru.cqa.api.validation.GreetingValidatorException;
import ru.cqa.api.validation.SaveAndMergeValidatorException;
import ru.cqa.api.validation.Validator;
import ru.cqa.cases.mdm.validation.rules.greeting.GreetingValidationRules;
import ru.cqa.cases.mdm.validation.rules.party.legal.LegalPartyValidationRules;
import ru.cqa.cases.mdm.validation.rules.party.physical.PhysicalPartyValidationRules;
import ru.cqa.model.greeting.OperatorDTO;
import ru.cqa.model.mdm.FieldDTO;
import ru.cqa.model.mdm.PartyDTO;
import ru.cqa.model.mdm.SaveAndMergeDTO;
import ru.cqa.validation.BeanFormValidator;
import ru.cqa.validators.GreetingValidator;
import ru.cqa.validators.mdm.PartyTypeValidator;
import ru.cqa.validators.mdm.SaveAndMergeValidator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.cqa.cases.mdm.constants.PartyType.LEGAL;
import static ru.cqa.cases.mdm.constants.PartyType.PHYSICAL;

class ValidationContractTest {
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private final FormValidator formValidator = new BeanFormValidator(FACTORY.getValidator());

    @AfterAll
    static void closeProvider() {
        FACTORY.close();
    }

    @Test
    void formAndBusinessErrorsAreReturnedTogetherInStableOrder() {
        Validator<OperatorDTO> validator = new GreetingValidator(formValidator, new GreetingValidationRules().rules());
        var error = assertThrows(GreetingValidatorException.class,
                () -> validator.validateBody(new OperatorDTO("Ivan", "")));
        assertEquals(List.of("family: Не должно быть пустым",
                "name: только русские буквы, первая заглавная, остальные строчные"), error.getErrors());
        assertDoesNotThrow(() -> validator.validateBody(new OperatorDTO("Иван", "Петров")));
    }

    @Test
    void formErrorsKeepNestedPathsAndSorting() {
        var party = new PartyDTO("LEGAL", "", List.of(new FieldDTO("", "value")), "", null);
        assertEquals(List.of(
                "party[0].field[0].name: Не должно быть пустым",
                "party[0].rawId: Не должно быть пустым",
                "party[0].sourceSystem: Не должно быть пустым",
                "updateType: Не должно быть пустым"),
                formValidator.validate(new SaveAndMergeDTO(List.of(party), null, "")));
    }

    @Test
    void partyTypeSelectsTheCorrectBusinessRules() {
        Validator<SaveAndMergeDTO> validator = new PartyTypeValidator(Map.of(
                PHYSICAL, new SaveAndMergeValidator(formValidator, new PhysicalPartyValidationRules().rules()),
                LEGAL, new SaveAndMergeValidator(formValidator, new LegalPartyValidationRules().rules())));
        assertDoesNotThrow(() -> validator.validateBody(request("LEGAL")));
        var physical = assertThrows(SaveAndMergeValidatorException.class,
                () -> validator.validateBody(request("PHYSICAL")));
        assertTrue(physical.getErrors().stream().anyMatch(e -> e.startsWith("party[0].field.birthPlace:")));
        var unknown = assertThrows(SaveAndMergeValidatorException.class,
                () -> validator.validateBody(request("OTHER")));
        assertTrue(unknown.getErrors().getFirst().startsWith("party[0].type:"));
    }

    private static SaveAndMergeDTO request(String type) {
        return new SaveAndMergeDTO(List.of(new PartyDTO(type, "TEST", List.of(
                new FieldDTO("identificationLevel", "FULL"),
                new FieldDTO("createdDate", "15.03.2024"),
                new FieldDTO("actualityDate", "15.03.2024 10:20:30")), "123", null)), null, "FULL");
    }
}
