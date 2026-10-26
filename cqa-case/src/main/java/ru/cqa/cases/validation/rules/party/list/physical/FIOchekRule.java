package ru.cqa.cases.validation.rules.party.list.physical;

import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.dto.PartyDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.cqa.cases.validation.constants.RequisitesNames.*;

/**
 * 7. Фамилия, Имя, Отчество передаётся только в {@code name}, {@code surname}, {@code patronymic}
 * ИЛИ только в {@code fullNameRawSource}.
 *
 * <p>ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица.
 */
public final class FIOchekRule  implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        Map<String, String> f = party.fieldMap();
        List<String> fio = List.of(NAME, SURNAME, PATRONYMIC);

        boolean fioAll = fio.stream().noneMatch(n -> isBlank(f.get(n)));
        boolean fioAny = fio.stream().anyMatch(n -> !isBlank(f.get(n)));
        boolean full = !isBlank(f.get(FULL_NAME_RAW_SOURCE));

        // TODO уточнить вопрос по обязательности отчества!!!
        //  пока поле отчество обязательно и требуется заполнить его не пустым

        if (full && fioAny) {
            return List.of("party[0].field: заполнено и ФИО, и " + FULL_NAME_RAW_SOURCE + " — допустимо что-то одно");
        }
        if (!full && !fioAny) {
            return List.of("party[0].field: ожидается либо " + String.join(", ", fio) + ", либо " + FULL_NAME_RAW_SOURCE);
        }
        if (!full && !fioAll) {
            List<String> errors = new ArrayList<>();
            for (String n : fio) {
                if (isBlank(f.get(n))) {
                    errors.add("party[0].field." + n + ": не должно быть пустым");
                }
            }
            return errors;
        }
        return List.of();
    }
}
