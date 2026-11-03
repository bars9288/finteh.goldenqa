package ru.cqa.cases.grerting;

public interface GreetingService<T> {

    Class<T> subjectType();

    String greet(T subject);
}
