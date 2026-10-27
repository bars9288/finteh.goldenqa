package ru.cqa.businesslogic.greeting;

public interface GreetingService<T> {

    Class<T> subjectType();

    String greet(T subject);
}
