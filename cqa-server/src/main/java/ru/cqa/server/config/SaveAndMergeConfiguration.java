package ru.cqa.server.config;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.cqa.cases.mdm.constants.PartyType;
import ru.cqa.cases.mdm.validation.rules.party.legal.LegalPartySaveRules;
import ru.cqa.cases.mdm.validation.rules.party.physical.PhysicalPartySaveRules;
import ru.cqa.validators.mdm.SaveAndMergeValidator;

import static ru.cqa.cases.mdm.constants.PartyType.LEGAL;
import static ru.cqa.cases.mdm.constants.PartyType.PHYSICAL;

@Configuration
public class SaveAndMergeConfiguration {

    Logger logger = LoggerFactory.getLogger(SaveAndMergeConfiguration.class);

    @Bean
    public Map<PartyType, SaveAndMergeValidator> saveAndMergeValidators(jakarta.validation.Validator validator) {
        logger.info("Инициализация правил и валидации ../saveAndMerge");
        return Map.of(
                PHYSICAL, new SaveAndMergeValidator(validator, new PhysicalPartySaveRules().ruleValues()),
                LEGAL, new SaveAndMergeValidator(validator, new LegalPartySaveRules().ruleValues()));
    }
}
