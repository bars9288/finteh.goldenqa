package ru.cqa.business.greeting;

public interface GreetingService<T> {

    Class<T> subjectType();

    String greet(T subject);
}
