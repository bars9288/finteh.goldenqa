package ru.cqa.cases.mdm.validation.rules.party.physical;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ru.cqa.api.validation.Rule;
import ru.cqa.model.mdm.PartyDTO;
import ru.cqa.utils.FieldMaps;

import static ru.cqa.cases.mdm.constants.PartyFieldNames.*;

/**
 * 7. Фамилия, Имя, Отчество передаётся только в {@code name}, {@code surname}, {@code patronymic}
 * ИЛИ только в {@code fullNameRawSource}.
 *
 * <p>ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица.
 */
public final class FullNameRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        Map<String, String> fieldsByName = FieldMaps.of(party);
        List<String> nameFields = List.of(NAME, SURNAME, PATRONYMIC);

        boolean hasCompleteName = nameFields.stream().noneMatch(fieldName -> isBlank(fieldsByName.get(fieldName)));
        boolean hasAnyNamePart = nameFields.stream().anyMatch(fieldName -> !isBlank(fieldsByName.get(fieldName)));
        boolean hasFullNameRawSource = !isBlank(fieldsByName.get(FULL_NAME_RAW_SOURCE));

        // TODO уточнить вопрос по обязательности отчества!!!
        //  пока поле отчество обязательно и требуется заполнить его не пустым

        if (hasFullNameRawSource && hasAnyNamePart) {
            return List.of("party[0].field: заполнено и ФИО, и " + FULL_NAME_RAW_SOURCE + " — допустимо что-то одно");
        }
        if (!hasFullNameRawSource && !hasAnyNamePart) {
            return List.of("party[0].field: ожидается либо " + String.join(", ", nameFields) + ", либо " + FULL_NAME_RAW_SOURCE);
        }
        if (!hasFullNameRawSource && !hasCompleteName) {
            List<String> errors = new ArrayList<>();
            for (String fieldName : nameFields) {
                if (isBlank(fieldsByName.get(fieldName))) {
                    errors.add("party[0].field." + fieldName + ": не должно быть пустым");
                }
            }
            return errors;
        }
        return List.of();
    }
}
