package ru.cqa.scenario.connection;

/** Находит ссылку на подключение во внешней конфигурации CQA. */
public interface ConnectionResolver {
    ConnectionDefinition resolve(String connectionId);
}
