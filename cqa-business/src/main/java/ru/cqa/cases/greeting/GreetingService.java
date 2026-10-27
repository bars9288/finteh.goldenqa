package ru.cqa.cases.greeting;

public interface GreetingService<T> {

    Class<T> subjectType();

    String greet(T subject);
}
