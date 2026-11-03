package ru.cqa.scenario.connection;

/** Инфраструктурный адаптер создаёт handle для своего типа подключения. */
public interface ConnectionProvider {
    String getType();

    ConnectionHandle createConnection(ConnectionDefinition definition);
}
