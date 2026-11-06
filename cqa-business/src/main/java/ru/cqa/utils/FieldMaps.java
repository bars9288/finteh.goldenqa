package ru.cqa.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ru.cqa.model.mdm.AttributeDTO;
import ru.cqa.model.mdm.FieldDTO;
import ru.cqa.model.mdm.PartyDTO;

/** Поля участника или атрибута в виде «имя → значение»: правилам удобнее искать по имени, чем перебирать список. */
public final class FieldMaps {

    private FieldMaps() {
    }

    public static Map<String, String> of(PartyDTO party) {
        return toMap(party.field());
    }

    public static Map<String, String> of(AttributeDTO attribute) {
        return toMap(attribute.field());
    }

    private static Map<String, String> toMap(List<FieldDTO> fields) {
        Map<String, String> valuesByName = new LinkedHashMap<>();
        if (fields != null) {
            for (FieldDTO field : fields) {
                if (field != null) {
                    valuesByName.put(field.name(), field.value());
                }
            }
        }
        return valuesByName;
    }
}
