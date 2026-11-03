package ru.cqa.scenario.connection;

/** Нейтральный к транспорту handle; конкретный адаптер владеет его ресурсом. */
public interface ConnectionHandle extends AutoCloseable {
    @Override
    void close();
}
