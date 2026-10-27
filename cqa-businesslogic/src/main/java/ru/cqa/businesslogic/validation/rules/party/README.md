# Валидация по правилам бизнес-логики

Правила проверки запроса `saveAndMerge`, которые не выразить аннотациями DTO.
Применяются к `party[0]` после проверки формы (`@NotBlank`, `@NotEmpty` и т.п.,
см. [README_JakartaValid](README_JakartaValid.md)); ошибки обоих слоёв возвращаются
клиенту одним списком.

Наборы правил:

| Тип участника   | Набор                                                | Состав                       |
|-----------------|------------------------------------------------------|------------------------------|
| Физическое лицо | [PhysicalPartySaveRules](physical/PhysicalPartySaveRules.java) | общие 1–6 + частные 7–22     |
| Юридическое лицо| [LegalPartySaveRules](legal/LegalPartySaveRules.java) | общие 1–6 + частное 31        |

Общие правила собраны в [PartyRules](PartyRules.java). Номер правила в этом файле
совпадает с номером в комментарии класса.

Классы лежат по папкам: `common/` — правила, которые подходят любому участнику, `physical/` и
`legal/` — частные правила и набор своего типа. Правила 20–22 (адрес, телефон, email) лежат
в `common/`, потому что пригодятся и юрлицам, но сейчас подключены только в наборе ФЛ.

Набор выбирается по `party[0].type`: `PHYSICAL` — ФЛ, `LEGAL` — ЮЛ; другой тип — ошибка
`party[0].type: ожидается одно из [PHYSICAL, LEGAL]`, и правила не запускаются.

**Условные обозначения**

- **реквизит** — элемент `field` (`{"name": "...", "value": "..."}`) участника или атрибута;
- **пусто** — реквизита нет, `value` равно `null`, `""` или одним пробелам. Правила
  не отличают отсутствие реквизита от пустого значения;
- значения перед проверкой обрезаются по краям (`strip`), если не сказано иное;
- **тип атрибута** (`attribute.type`) и **реквизит `type` атрибута** (`attribute.field[name=type]`) — разные вещи:

```jsonc
"attribute": [{
  "type": "DOCUMENT_PASSPORT",                       // тип атрибута
  "rawId": "12",
  "field": [
    {"name": "type", "value": "PASSPORT"},           // реквизит type
    {"name": "actualityDate", "value": "15.03.2024 10:20:30"}
  ]
}]
```

**Примеры к правилам** — фрагменты `party[0]`: `✅` проходит правило, `❌` — нет, в комментарии
текст ошибки. В примерах `❌` показаны только реквизиты разбираемого правила, остальные опущены.

**Обязательные реквизиты** — без них запрос не пройдёт. Всё, чего нет в списке, необязательно:
если реквизит не передан или пустой, правило его пропускает.

| Где | Реквизит | Кому | Правило |
|---|---|---|---|
| участник | `identificationLevel` | ФЛ, ЮЛ | 1 |
| участник | `createdDate` | ФЛ, ЮЛ | 2 |
| участник | `actualityDate` | ФЛ, ЮЛ | 3 |
| каждый атрибут | `type` | ФЛ, ЮЛ | 4 |
| каждый атрибут | `actualityDate` | ФЛ, ЮЛ | 6 |
| участник | `surname` + `name` + `patronymic` **или** `fullNameRawSource` | ФЛ | 7 |
| участник | `birthPlace` | ФЛ | 11 |
| атрибут `DOCUMENT_PASSPORT` | `type` = `PASSPORT`, `qualifierType` | ФЛ | 19 |
| атрибут `PHONE` | `rawSource` | ФЛ | 21 |
| атрибут `EMAIL` | `rawSource` | ФЛ | 22 |

Обратное тоже есть — реквизиты, которые **не заполняются**: `primary` (5), `birthdate` (8),
`gender` (10), `inn` (14), `snils` (15), `rawSource` у паспорта (19).

---

<details>
<summary><h2>Общие правила (ФЛ и ЮЛ)</h2></summary>

### 1. Уровень идентификации
[IdentificationLevelRule](common/IdentificationLevelRule.java)

Реквизит `identificationLevel` обязателен. Значение не проверяется — достаточно, чтобы оно было непустым.

| identificationLevel | Итог |
|---|---|
| нет реквизита / `""` / `"  "` | ❌ обязательно, не должно быть пустым |
| `"FULL"`, `"abc"` | ✅ |

```jsonc
// ✅
"field": [ {"name": "identificationLevel", "value": "FULL"} ]

// ❌ party[0].field.identificationLevel: обязательно, не должно быть пустым
"field": [ {"name": "identificationLevel", "value": "  "} ]
```

### 2. Дата создания
[CreateddateRule](common/CreateddateRule.java)

Реквизит `createdDate` обязателен. Формат — `ДД.ММ.ГГГГ` или `ДД.ММ.ГГГГ ЧЧ:мм:СС [пояс]`.
Дата проверяется по календарю; пояс необязателен, но если указан — только московский:
`МСК`, `MSK`, `+03:00`, `Europe/Moscow`.

| createdDate | Итог |
|---|---|
| `15.03.2024` | ✅ |
| `15.03.2024 10:20:30` / `15.03.2024 10:20:30 МСК` | ✅ |
| пусто | ❌ обязательно |
| `31.02.2024` (нет такой даты) | ❌ формат |
| `15.03.2024 10:20` (без секунд) | ❌ формат |
| `15.03.2024 МСК` (пояс без времени) | ❌ формат |
| `15.03.2024  10:20:30` (два пробела) | ❌ формат |
| `15.03.2024⇥10:20:30` (табуляция вместо пробела) | ✅ — разделителем считается любой пробельный символ |
| `15.03.2024 10:20:30 МСК 1` (лишняя часть) | ❌ формат |
| `15.03.2024 10:20:30 UTC` / `msk` | ❌ пояс должен быть московский (регистр важен) |

```jsonc
// ✅
"field": [ {"name": "createdDate", "value": "15.03.2024"} ]
"field": [ {"name": "createdDate", "value": "15.03.2024 10:20:30 МСК"} ]

// ❌ party[0].field.createdDate: ожидается дата в формате ДД.ММ.ГГГГ или ДД.ММ.ГГГГ ЧЧ:мм:СС в Московском часовом поясе
"field": [ {"name": "createdDate", "value": "31.02.2024"} ]

// ❌ party[0].field.createdDate: часовой пояс должен быть московский: МСК, MSK, +03:00, Europe/Moscow
"field": [ {"name": "createdDate", "value": "15.03.2024 10:20:30 UTC"} ]
```

### 3. Дата актуальности участника
[ActualitydateRule](common/ActualitydateRule.java)

Реквизит `actualityDate` участника обязателен. Формат — только с временем:
`ДД.ММ.ГГГГ ЧЧ:мм:СС [пояс]`, правила пояса — как в п. 2.

| actualityDate | Итог |
|---|---|
| `15.03.2024 10:20:30` / `... +03:00` | ✅ |
| `15.03.2024⇥10:20:30` (табуляция вместо пробела) | ✅ — разделитель любой пробельный символ |
| `15.03.2024` (без времени) | ❌ формат |
| пусто | ❌ обязательно |
| `15.03.2024 10:20:30 GMT` | ❌ пояс должен быть московский |

```jsonc
// ✅
"field": [ {"name": "actualityDate", "value": "15.03.2024 10:20:30 +03:00"} ]

// ❌ party[0].field.actualityDate: ожидается дата и время в формате ДД.ММ.ГГГГ ЧЧ:мм:СС, после времени — необязательный пояс
"field": [ {"name": "actualityDate", "value": "15.03.2024"} ]
```

### 4. Реквизит type у каждого атрибута
[AttributeTypeRule](common/AttributeTypeRule.java)

У каждого атрибута обязателен **реквизит** `type` (не путать с типом атрибута).
Атрибутов нет — проверять нечего.

| Атрибут | Итог |
|---|---|
| `field` содержит `type` с непустым значением | ✅ |
| реквизита `type` нет или он пустой | ❌ `attribute[i].field.type`: обязательно |
| список `attribute` не передан | ✅ |

```jsonc
// ✅
"attribute": [{ "type": "ADDRESS", "rawId": "1",
  "field": [ {"name": "type", "value": "REGISTRATION"} ] }]

// ❌ party[0].attribute[0].field.type: обязательно, не должно быть пустым
"attribute": [{ "type": "PHONE", "rawId": "1",
  "field": [ {"name": "rawSource", "value": "+7 900 123-45-67"} ] }]
```

### 5. Реквизит primary не заполняется
[PrimaryRule](common/PrimaryRule.java)

Ни у одного атрибута реквизит `primary` не должен быть заполнен — признак основного
атрибута выставляет МДМ, а не источник.

| primary | Итог |
|---|---|
| нет реквизита / `""` | ✅ |
| `"true"`, `"false"` — любое значение | ❌ не заполняется |

```jsonc
// ✅
"attribute": [{ "type": "PHONE", "rawId": "2",
  "field": [ {"name": "primary", "value": ""} ] }]

// ❌ party[0].attribute[0].field.primary: не заполняется
"attribute": [{ "type": "PHONE", "rawId": "2",
  "field": [ {"name": "primary", "value": "true"} ] }]
```

### 6. Дата актуальности атрибута
[ActualitydateAttrRule](common/ActualitydateAttrRule.java)

Если атрибуты переданы, у каждого из них обязателен реквизит `actualityDate`;
формат и пояс — как в п. 3. Нет атрибутов — проверять нечего.

| actualityDate атрибута | Итог |
|---|---|
| `15.03.2024 10:20:30 MSK` | ✅ |
| `15.03.2024⇥10:20:30` (табуляция вместо пробела) | ✅ — как в п. 2–3, разделитель любой пробельный символ |
| пусто | ❌ обязательно |
| `15.03.2024` | ❌ формат |
| список `attribute` не передан или пустой | ✅ |

```jsonc
// ✅
"attribute": [{ "type": "EMAIL", "rawId": "3",
  "field": [ {"name": "actualityDate", "value": "15.03.2024 10:20:30 MSK"} ] }]

// ❌ party[0].attribute[0].field.actualityDate: обязательно, не должно быть пустым
"attribute": [{ "type": "EMAIL", "rawId": "3",
  "field": [ {"name": "actualityDate", "value": null} ] }]
```

</details>

---

<details>
<summary><h2>Физические лица</h2></summary>

Набор [PhysicalPartySaveRules](physical/PhysicalPartySaveRules.java): общие правила 1–6 и частные ниже.

Полный запрос ФЛ, который проходит все правила:

```jsonc
{
  "party": [{
    "type": "PHYSICAL",
    "sourceSystem": "SBL",
    "rawId": "123456789abcdef",
    "field": [
      {"name": "identificationLevel",         "value": "FULL"},
      {"name": "createdDate",                 "value": "15.03.2024"},
      {"name": "actualityDate",               "value": "15.03.2024 10:20:30 МСК"},
      {"name": "surname",                     "value": "Иванов"},
      {"name": "name",                        "value": "Иван"},
      {"name": "patronymic",                  "value": "Иванович"},
      {"name": "birthdateRawSource",          "value": "15.05.1990"},
      {"name": "genderRawSource",             "value": "MALE"},
      {"name": "birthPlace",                  "value": "г. Казань"},
      {"name": "citizenship",                 "value": "643"},
      {"name": "foreignMonetaryResidentFlag", "value": "TRUE"},
      {"name": "innRawSource",                "value": "165012345678"},
      {"name": "snilsRawSource",              "value": "123-456-789 01"},
      {"name": "vip",                         "value": "FALSE"},
      {"name": "bankEmployeeFlag",            "value": "FALSE"}
    ],
    "attribute": [
      { "type": "DOCUMENT_PASSPORT", "rawId": "10",
        "field": [
          {"name": "type",          "value": "PASSPORT"},
          {"name": "qualifierType", "value": "RF"},
          {"name": "actualityDate", "value": "15.03.2024 10:20:30"}
        ] },
      { "type": "EMAIL", "rawId": "11",
        "field": [
          {"name": "type",          "value": "PERSONAL"},
          {"name": "rawSource",     "value": "ivanov@bank.ru"},
          {"name": "actualityDate", "value": "15.03.2024 10:20:30"}
        ] }
    ]
  }],
  "include": { "partyInfo": ["SOURCE", "REQUISITE", "ATTRIBUTE"] },
  "updateType": "FULL"
}
```

### Реквизиты

#### 7. ФИО
[FIOchekRule](physical/FIOchekRule.java)

ФИО передаётся **либо** тремя реквизитами `surname`, `name`, `patronymic`, **либо**
одной строкой в `fullNameRawSource` — но не одновременно.
⚠️ Обязательность отчества уточняется; пока при раздельном ФИО оно обязательно и непустое.

| surname / name / patronymic | fullNameRawSource | Итог |
|---|---|---|
| все три непустые | пусто | ✅ |
| все пусто или нет реквизитов | непустое | ✅ |
| хотя бы одно пустое или отсутствует | пусто | ❌ по каждому пустому: не должно быть пустым |
| хотя бы одно непустое | непустое | ❌ допустимо что-то одно |
| все пусто | пусто | ❌ ожидается либо ФИО, либо fullNameRawSource |

```jsonc
// ✅ раздельное ФИО
"field": [
  {"name": "surname",    "value": "Иванов"},
  {"name": "name",       "value": "Иван"},
  {"name": "patronymic", "value": "Иванович"}
]

// ✅ одной строкой
"field": [ {"name": "fullNameRawSource", "value": "Иванов Иван Иванович"} ]

// ❌ party[0].field.patronymic: не должно быть пустым
"field": [
  {"name": "surname", "value": "Иванов"},
  {"name": "name",    "value": "Иван"}
]

// ❌ party[0].field: заполнено и ФИО, и fullNameRawSource — допустимо что-то одно
"field": [
  {"name": "surname",           "value": "Иванов"},
  {"name": "fullNameRawSource", "value": "Иванов Иван Иванович"}
]
```

#### 8. Дата рождения
[BirthdayDateRule](physical/BirthdayDateRule.java)

Дата рождения передаётся только в `birthdateRawSource` в формате `ДД.ММ.ГГГГ`;
реквизит `birthdate` не заполняется — любое значение в нём ошибка.
Ни один из реквизитов не обязателен.

| birthdate | birthdateRawSource | Итог |
|---|---|---|
| пусто | `15.05.1990` | ✅ |
| пусто | пусто | ✅ |
| `15.05.1990` / `1990-05-15` / `abc` — любое | любое | ❌ birthdate не заполняется |
| пусто | `31.02.1990` / `1990-05-15` | ❌ ожидается ДД.ММ.ГГГГ |

```jsonc
// ✅
"field": [ {"name": "birthdateRawSource", "value": "15.05.1990"} ]

// ❌ party[0].field.birthdate: дату в формате ДД.ММ.ГГГГ указывайте в birthdateRawSource
"field": [ {"name": "birthdate", "value": "1990-05-15"} ]

// ❌ party[0].field.birthdateRawSource: ожидается дата в формате ДД.ММ.ГГГГ
"field": [ {"name": "birthdateRawSource", "value": "31.02.1990"} ]
```

#### 9. Дата смерти
[DeathDateRule](physical/DeathDateRule.java)

Реквизит `deathDate` необязателен; если заполнен — формат `ДД.ММ.ГГГГ` и дата строго
позже `birthdateRawSource`. Если дата рождения пустая или кривая, сравнение пропускается.

| deathDate | birthdateRawSource | Итог |
|---|---|---|
| пусто | любое | ✅ |
| `01.01.2020` | `15.05.1990` | ✅ |
| `15.05.1990` | `15.05.1990` (та же дата) | ❌ позже даты рождения |
| `01.01.1980` | `15.05.1990` | ❌ позже даты рождения |
| `2020-01-01` | любое | ❌ формат |
| `31.02.2020` (нет такой даты) | любое | ❌ формат |
| `01.01.2020` | пусто | ✅ |
| `01.01.1980` | `31.02.1990` (кривая) | ✅ здесь — сравнение пропущено; ошибку даст п. 8 |
| `01.01.2099` (в будущем) | `15.05.1990` | ✅ — будущая дата не проверяется |

```jsonc
// ✅
"field": [
  {"name": "birthdateRawSource", "value": "15.05.1990"},
  {"name": "deathDate",          "value": "01.01.2020"}
]

// ❌ party[0].field.deathDate: дата смерти должна быть позже даты рождения
"field": [
  {"name": "birthdateRawSource", "value": "15.05.1990"},
  {"name": "deathDate",          "value": "15.05.1990"}
]

// ❌ party[0].field.deathDate: ожидается дата в формате ДД.ММ.ГГГГ
"field": [ {"name": "deathDate", "value": "2020-01-01"} ]
```

#### 10. Пол
[GenderRule](physical/GenderRule.java)

Пол передаётся только в `genderRawSource`: `MALE` или `FEMALE`, регистр не важен.
Реквизит `gender` не заполняется. Пол необязателен.

| gender | genderRawSource | Итог |
|---|---|---|
| пусто | `MALE` / `female` | ✅ |
| пусто | пусто | ✅ |
| `MALE` | любое | ❌ gender не заполняется |
| пусто | `M` / `МУЖ` | ❌ допустимо MALE или FEMALE |

```jsonc
// ✅
"field": [ {"name": "genderRawSource", "value": "female"} ]

// ❌ party[0].field.gender: не заполняется, пол указывайте в genderRawSource
"field": [ {"name": "gender", "value": "MALE"} ]

// ❌ party[0].field.genderRawSource: допустимо MALE или FEMALE
"field": [ {"name": "genderRawSource", "value": "M"} ]
```

#### 11. Место рождения
[BirthPlaceRule](physical/BirthPlaceRule.java)

Реквизит `birthPlace` обязателен. `birthPlaceCountry`, `birthPlaceRegion` и подобные
его не заменяют — место рождения читается только из `birthPlace`.

| birthPlace | Итог |
|---|---|
| `г. Казань` | ✅ |
| пусто (даже если есть birthPlaceCountry) | ❌ не должно быть пустым |

```jsonc
// ✅
"field": [ {"name": "birthPlace", "value": "г. Казань"} ]

// ❌ party[0].field.birthPlace: не должно быть пустым — место рождения читается только из birthPlace
"field": [ {"name": "birthPlaceCountry", "value": "Россия"} ]
```

#### 12. Гражданство
[CitizenshipRule](physical/CitizenshipRule.java)

Реквизит `citizenship` необязателен; если заполнен — код из 1–3 цифр или `UNKNOWN`.

| citizenship | Итог |
|---|---|
| пусто | ✅ |
| `2` / `12` / `643` | ✅ |
| `UNKNOWN` | ✅ |
| `unknown` (регистр) | ❌ |
| `0643` / `RUS` | ❌ от одной до трёх цифр или UNKNOWN |

```jsonc
// ✅
"field": [ {"name": "citizenship", "value": "643"} ]
"field": [ {"name": "citizenship", "value": "UNKNOWN"} ]

// ❌ party[0].field.citizenship: ожидается от одной до трёх цифр или UNKNOWN
"field": [ {"name": "citizenship", "value": "RUS"} ]
```

#### 13. Валютное резидентство
[ForMonetResidRule](physical/ForMonetResidRule.java)

Реквизит `foreignMonetaryResidentFlag` необязателен; значения `TRUE`, `FALSE`, `UNKNOWN`.
**Регистр важен** — как у VIP (п. 16) и признака сотрудника банка (п. 17).

| foreignMonetaryResidentFlag | Итог |
|---|---|
| пусто / `TRUE` / `FALSE` / `UNKNOWN` | ✅ |
| `false` / `Unknown` (регистр) | ❌ допустимо TRUE, FALSE или UNKNOWN |
| `1` / `YES` | ❌ допустимо TRUE, FALSE или UNKNOWN |

```jsonc
// ✅
"field": [ {"name": "foreignMonetaryResidentFlag", "value": "FALSE"} ]

// ❌ party[0].field.foreignMonetaryResidentFlag: допустимо TRUE, FALSE или UNKNOWN
"field": [ {"name": "foreignMonetaryResidentFlag", "value": "false"} ]
```

#### 14. ИНН
[InnRule](physical/InnRule.java)

ИНН передаётся только в `innRawSource`; реквизит `inn` не заполняется.
ИНН необязателен; формат `innRawSource` не проверяется.

| inn | innRawSource | Итог |
|---|---|---|
| пусто | `165012345678` / `abc` / пусто | ✅ |
| `165012345678` | любое | ❌ inn не заполняется |

```jsonc
// ✅
"field": [ {"name": "innRawSource", "value": "165012345678"} ]

// ❌ party[0].field.inn: не заполняется, ИНН указывайте в innRawSource
"field": [ {"name": "inn", "value": "165012345678"} ]
```

#### 15. СНИЛС
[SnilsRule](physical/SnilsRule.java)

СНИЛС передаётся только в `snilsRawSource` в формате `123-456-789 01`; реквизит `snils`
не заполняется. СНИЛС необязателен; контрольное число не проверяется.

| snils | snilsRawSource | Итог |
|---|---|---|
| пусто | `123-456-789 01` / пусто | ✅ |
| пусто | `000-000-000 00` | ✅ — контрольное число не проверяется |
| пусто | `123-456-789⇥01` (табуляция вместо пробела) | ✅ — разделитель перед последней парой любой пробельный символ |
| пусто | `12345678901` / `123-456-789-01` / `123-456-789  01` | ❌ ожидается формат '123-123-123 12' |
| `123-456-789 01` | любое | ❌ snils не заполняется |

```jsonc
// ✅
"field": [ {"name": "snilsRawSource", "value": "123-456-789 01"} ]

// ❌ party[0].field.snilsRawSource: ожидается формат '123-123-123 12'
"field": [ {"name": "snilsRawSource", "value": "12345678901"} ]

// ❌ party[0].field.snils: не заполняется, СНИЛС указывайте в snilsRawSource
"field": [ {"name": "snils", "value": "123-456-789 01"} ]
```

#### 16. VIP
[VipRule](physical/VipRule.java)

Реквизит `vip` необязателен; значения `TRUE`, `FALSE`, `UNKNOWN`.
**Регистр важен** — как у валютного резидентства (п. 13) и признака сотрудника банка (п. 17).

| vip | Итог |
|---|---|
| пусто / `TRUE` / `FALSE` / `UNKNOWN` | ✅ |
| `true` / `Unknown` (регистр) | ❌ допустимо TRUE, FALSE или UNKNOWN |
| `1` / `YES` | ❌ допустимо TRUE, FALSE или UNKNOWN |

```jsonc
// ✅
"field": [ {"name": "vip", "value": "TRUE"} ]

// ❌ party[0].field.vip: допустимо TRUE, FALSE или UNKNOWN
"field": [ {"name": "vip", "value": "true"} ]
```

#### 17. Сотрудник банка
[BankEmployeeRule](physical/BankEmployeeRule.java)

Реквизит `bankEmployeeFlag` необязателен; значения `TRUE`, `FALSE`, `UNKNOWN`.
**Регистр важен** — как у валютного резидентства (п. 13) и VIP (п. 16).

| bankEmployeeFlag | Итог |
|---|---|
| пусто / `TRUE` / `FALSE` / `UNKNOWN` | ✅ |
| `false` / `Unknown` (регистр) | ❌ допустимо TRUE, FALSE или UNKNOWN |
| `1` / `Y` | ❌ допустимо TRUE, FALSE или UNKNOWN |

```jsonc
// ✅
"field": [ {"name": "bankEmployeeFlag", "value": "FALSE"} ]

// ❌ party[0].field.bankEmployeeFlag: допустимо TRUE, FALSE или UNKNOWN
"field": [ {"name": "bankEmployeeFlag", "value": "false"} ]
```

### Атрибуты

#### 18. Допустимые типы атрибутов
[AttributePhysicFIlterRule](physical/AttributePhysicFIlterRule.java)

Тип каждого атрибута (`attribute.type`) — из списка: `DOCUMENT_PASSPORT`, `ADDRESS`,
`PHONE`, `EMAIL`, `EXTERNAL_IDENTIFIER`. Регистр важен. Все нарушения собираются
в одно сообщение без номера атрибута.

| attribute.type | Итог |
|---|---|
| `ADDRESS`, `EMAIL` | ✅ |
| `PASSPORT` / `address` | ❌ недопустимые типы: PASSPORT, address |
| пусто или атрибут `null` | ❌ недопустимы пустые и null значения |
| атрибутов нет | ✅ |

```jsonc
// ✅
"attribute": [
  { "type": "ADDRESS", "rawId": "1", "field": [ ... ] },
  { "type": "EMAIL",   "rawId": "2", "field": [ ... ] }
]

// ❌ party[0].attribute[i]. Недопустимые типы для Атрибута ФЛ: PASSPORT, address
"attribute": [
  { "type": "PASSPORT", "rawId": "1", "field": [ ... ] },
  { "type": "address",  "rawId": "2", "field": [ ... ] }
]
```

#### 19. Паспорт
[AttributeDocumentRule](physical/AttributeDocumentRule.java)

Для атрибута `DOCUMENT_PASSPORT`: реквизит `type` равен `PASSPORT`, реквизит
`qualifierType` обязателен, реквизит `rawSource` пустой.

| type | qualifierType | rawSource | Итог |
|---|---|---|---|
| `PASSPORT` | `RF` | пусто | ✅ |
| пусто / `passport` | `RF` | пусто | ❌ ожидается только PASSPORT (пустой `type` — ещё и ошибка п. 4) |
| `PASSPORT` | пусто | пусто | ❌ qualifierType обязательно |
| `PASSPORT` | `RF` | `9204 123456` | ❌ rawSource должно быть пустым |

```jsonc
// ✅
"attribute": [{ "type": "DOCUMENT_PASSPORT", "rawId": "10",
  "field": [
    {"name": "type",          "value": "PASSPORT"},
    {"name": "qualifierType", "value": "RF"}
  ] }]

// ❌ party[0].attribute[0].field.type: для DOCUMENT_PASSPORT ожидается только PASSPORT
// ❌ party[0].attribute[0].field.qualifierType: обязательно для DOCUMENT_PASSPORT
// ❌ party[0].attribute[0].field.rawSource: должно быть пустым
"attribute": [{ "type": "DOCUMENT_PASSPORT", "rawId": "10",
  "field": [
    {"name": "type",      "value": "passport"},
    {"name": "rawSource", "value": "9204 123456"}
  ] }]
```

#### 20. Адрес
[AttributeAddressRule](common/AttributeAddressRule.java)

Для атрибута `ADDRESS` обязателен реквизит `type`. Сам адрес пока не проверяется.
⚠️ Повторяет п. 4: адрес без `type` получит две ошибки.

⚠️ **Открытый вопрос:** как должен передаваться адрес — **либо** строкой в `rawSource`,
**либо** набором полей по справочнику ГАР / ФИАС / КЛАДР (одно из двух). Пока не решено,
правило проверяет только реквизит `type`.

| Реквизит type у ADDRESS | Адрес | Итог |
|---|---|---|
| `REGISTRATION` | любой, в том числе не передан | ✅ |
| пусто | любой | ❌ обязательно для ADDRESS (+ ошибка п. 4) |

```jsonc
// ✅
"attribute": [{ "type": "ADDRESS", "rawId": "1",
  "field": [ {"name": "type", "value": "REGISTRATION"} ] }]

// ❌ party[0].attribute[0].field.type: обязательно для ADDRESS
// ❌ party[0].attribute[0].field.type: обязательно, не должно быть пустым   (п. 4)
"attribute": [{ "type": "ADDRESS", "rawId": "1",
  "field": [ {"name": "type", "value": ""} ] }]
```

#### 21. Телефон
[AttributePhoneRule](common/AttributePhoneRule.java)

Для атрибута `PHONE` номер передаётся строкой в реквизите `rawSource`, он обязателен.
Формат номера не проверяется.

| rawSource у PHONE | Итог |
|---|---|
| `+7 900 123-45-67` / `abc` | ✅ |
| пусто | ❌ обязательно для PHONE |
| атрибут с типом `phone` (регистр) | правило не применяется — ошибку даст п. 18 |

```jsonc
// ✅
"attribute": [{ "type": "PHONE", "rawId": "2",
  "field": [ {"name": "rawSource", "value": "+7 900 123-45-67"} ] }]

// ❌ party[0].attribute[0].field.rawSource: обязательно для PHONE
"attribute": [{ "type": "PHONE", "rawId": "2",
  "field": [ {"name": "type", "value": "MOBILE"} ] }]
```

#### 22. Электронная почта
[AttributeEmailRule](common/AttributeEmailRule.java)

Для атрибута `EMAIL` адрес передаётся в реквизите `rawSource`: обязателен и содержит `@`.
Других проверок формата нет.

| rawSource у EMAIL | Итог |
|---|---|
| `user@bank.ru` | ✅ |
| `@` / `a@` | ✅ — достаточно знака @ |
| `user.bank.ru` | ❌ ожидается адрес со знаком @ |
| пусто | ❌ обязательно для EMAIL |

```jsonc
// ✅
"attribute": [{ "type": "EMAIL", "rawId": "3",
  "field": [ {"name": "rawSource", "value": "user@bank.ru"} ] }]

// ❌ party[0].attribute[0].field.rawSource: для EMAIL ожидается адрес со знаком @
"attribute": [{ "type": "EMAIL", "rawId": "3",
  "field": [ {"name": "rawSource", "value": "user.bank.ru"} ] }]
```

</details>

---

<details>
<summary><h2>Юридические лица</h2></summary>

Набор [LegalPartySaveRules](legal/LegalPartySaveRules.java): общие правила 1–6 и частное 31.

### Реквизиты

_Пока нет._ Класс [NameRule](legal/NameRule.java) — заготовка: пока ничего не проверяет
и в набор не подключён.

### Атрибуты

#### 31. Допустимые типы атрибутов ЮЛ
[AttributeLegalFIlterRule](legal/AttributeLegalFIlterRule.java)

То же, что правило 18 для ФЛ, но со своим списком. Тип каждого атрибута (`attribute.type`) —
из списка: `ADDRESS`, `PHONE`, `EMAIL`, `EXTERNAL_IDENTIFIER`, `FLAG`, `TIN`, `CERTIFICATE`,
`DSMP`, `TAX_RESIDENCE`. Регистр важен. Паспорта (`DOCUMENT_PASSPORT`) у ЮЛ нет.
Все нарушения собираются в одно сообщение без номера атрибута.

| attribute.type | Итог |
|---|---|
| `TIN`, `ADDRESS` | ✅ |
| `DOCUMENT_PASSPORT` / `tin` | ❌ недопустимые типы: DOCUMENT_PASSPORT, tin |
| пусто или атрибут `null` | ❌ недопустимы пустые и null значения |
| атрибутов нет | ✅ |

```jsonc
// ✅
"attribute": [
  { "type": "TIN",     "rawId": "1", "field": [ ... ] },
  { "type": "ADDRESS", "rawId": "2", "field": [ ... ] }
]

// ❌ party[0].attribute[i]. Недопустимые типы для Атрибута ЮЛ: DOCUMENT_PASSPORT
"attribute": [
  { "type": "DOCUMENT_PASSPORT", "rawId": "1", "field": [ ... ] }
]
```

</details>
