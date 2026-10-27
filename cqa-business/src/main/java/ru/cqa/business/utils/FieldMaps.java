package ru.cqa.business.utils;

import ru.cqa.dto.AttributeDTO;
import ru.cqa.dto.FieldDTO;
import ru.cqa.dto.PartyDTO;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        Map<String, String> map = new LinkedHashMap<>();
        if (fields != null) {
            for (FieldDTO f : fields) {
                if (f != null) {
                    map.put(f.name(), f.value());
                }
            }
        }
        return map;
    }
}
