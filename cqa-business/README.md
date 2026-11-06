# CQA Business

Прикладные правила saveAndMerge, проверки приветствий и преобразование DTO в домен.
Внутренние зависимости: `cqa-api`, `cqa-model`. Spring, сетевых клиентов и зависимости
на реализацию `cqa-validation` нет.

- `GreetingValidator` и `SaveAndMergeValidator` реализуют `Validator<T>`:
  ошибки `FormValidator` объединяются с ошибками предметных правил.
- `PartyTypeValidator` выбирает набор правил PHYSICAL/LEGAL внутри бизнес-модуля.
- `DefaultGreetingFacade` реализует `GreetingFacade`, используя внутренние мапперы
  и реестр приветствий. REST-контроллер отдельно вызывает валидатор оператора;
  существующее поведение SOAP-приветствий сохранено.
- Правила находятся в `ru.cqa.cases.mdm.validation.rules`; DTO — в `ru.cqa.model`.

Создание реализаций и наборов правил выполняет `cqa-bootstrap`.

Описание проверок: [правила](src/main/java/ru/cqa/cases/mdm/validation/rules/party/README.md),
[ограничения формы](src/main/java/ru/cqa/cases/mdm/validation/rules/party/README_JakartaValid.md).

```bash
mvn -pl cqa-business -am package
```
