package ru.cqa.server.config;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.cqa.business.constants.PartyType;
import ru.cqa.business.validation.rules.party.legal.LegalPartySaveRules;
import ru.cqa.business.validation.rules.party.physical.PhysicalPartySaveRules;
import ru.cqa.business.validation.validators.SaveAndMergeValidator;

import static ru.cqa.business.constants.PartyType.*;

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
