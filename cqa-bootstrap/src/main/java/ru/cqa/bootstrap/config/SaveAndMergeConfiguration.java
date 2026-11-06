package ru.cqa.bootstrap.config;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.cqa.api.validation.FormValidator;
import ru.cqa.api.validation.Validator;
import ru.cqa.cases.mdm.validation.rules.party.legal.LegalPartyValidationRules;
import ru.cqa.cases.mdm.validation.rules.party.physical.PhysicalPartyValidationRules;
import ru.cqa.model.mdm.SaveAndMergeDTO;
import ru.cqa.validators.mdm.PartyTypeValidator;
import ru.cqa.validators.mdm.SaveAndMergeValidator;

import static ru.cqa.cases.mdm.constants.PartyType.LEGAL;
import static ru.cqa.cases.mdm.constants.PartyType.PHYSICAL;

@Configuration
public class SaveAndMergeConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SaveAndMergeConfiguration.class);

    @Bean
    public Validator<SaveAndMergeDTO> saveAndMergeValidator(FormValidator formValidator) {
        LOGGER.info("Инициализация правил и валидации ../saveAndMerge");
        return new PartyTypeValidator(Map.of(
                PHYSICAL, new SaveAndMergeValidator(formValidator, new PhysicalPartyValidationRules().rules()),
                LEGAL, new SaveAndMergeValidator(formValidator, new LegalPartyValidationRules().rules())));
    }
}
