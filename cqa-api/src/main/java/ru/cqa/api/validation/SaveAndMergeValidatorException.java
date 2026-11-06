package ru.cqa.api.validation;

import java.util.List;

public final class SaveAndMergeValidatorException extends ValidationException {

    public SaveAndMergeValidatorException(List<String> errors) {
        super(errors);
    }
}
