package ru.cqa.business.exceptions;

import java.util.List;

public final class SaveAndMergeValidationException extends ValidationException {

    public SaveAndMergeValidationException(List<String> errors) {
        super(errors);
    }
}
