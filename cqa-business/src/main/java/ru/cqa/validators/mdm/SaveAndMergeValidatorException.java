package ru.cqa.validators.mdm;

import ru.cqa.validation.ValidationException;

import java.util.List;

public final class SaveAndMergeValidatorException extends ValidationException {

    public SaveAndMergeValidatorException(List<String> errors) {
        super(errors);
    }
}
