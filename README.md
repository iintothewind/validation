[![Build Status](https://travis-ci.org/iintothewind/validation.svg?branch=master)](https://travis-ci.org/iintothewind/validation)

# com.github.iintothewind.Validation
A simple functional Validation framework

## Purpose

Consider there is an object instance `person` as following: 

```java
public class Person {
  private String name;
  private int age;
  private String address;
}
```

And the validation checks:

- the `person.name` should be equal to `John`
- and `person.age` should be greater than `18`
- and `person.address` should contains `China`

If any of the above checks failed, that validation should fail.

Too many times, I saw developers who can only understand Java 7 or older version language features 
wrote this kind of **shit** on production environment: 
```java
public void theLameValidation(final Person person) {
  if(person == null) {
    throw new ValidationException("person should not be null");
  }
  if(StringUtils.isEmpty(person.getName()) || !person.getName().equals("John")) {
    throw new ValidationException("person.name should not be empty");
  }
  if(person.getAge() == null || person.getAget() <= 18) {
    throw new ValidationException("person.age should be greater than 18");
  }
  if(person.getAddress() == null || !person.getAddress().contains("China")) {
    throw new ValidationException("person.address should contain China");
  }
}
```

It is obvious that the above validation is clumsy and not reusable:
- The validation breaks and exception throws when any check is failed. 
Consider this validation is used in a request check, and client just submitted with a request with a `Person` instance, with `name = Jack, Age = 12, Address = "US"`. 
The client users will try and fix at least 3 times to submit a correct request then they can finally pass the validation.
- Validation error is captured in exceptions. Validation user has to use try-catch to get the validation result, which makes code very ugly.
- The validation logic could vary in different scenarios. This method will not be useful for a different scenario


To resolve the above problems, the design purposes of this simple validation framework are:

- Return comprehensive failure messages when validation fails, not part of them
- Validations should be reusable
- Validations should be composable
- Zero external dependencies. All code is implemented by using jdk internal APIs.


## Design

```java
@FunctionalInterface
public interface Validation<T, E> {
  Iterable<E> validate(final T t);
  static <T, E> Validation<T, E> valid();
  default Validation<T, E> and(final Validation<? super T, ? extends E> other);
  default Validation<T, E> or(final Validation<? super T, ? extends E> other);
}
```

- The framework has only this simple functional interface.
And it takes any instance of type `T`, validates `t` instance and returns all failures with type `E`
- At very first, you can use `valid()` to start with a validation. `Valid()` means a validation that always success.
- After you created the first validation for type `T`, you can compose the other validation with function `and()` or `or()`



## Sample
By using this framework, the above validation could be:

First lets create three validations:

```java
final Validation<String, String> nameCheck = ValidationUtils.checkEqual("John");

final Validation<Integer, String> ageCheck = ValidationUtils.check(
  age -> Optional.ofNullable(age).filter(i -> i > 18).isPresent(),
  age -> String.format("person.age should be bigger than 18, but actual is %s", age));

final Validation<String, String> addressCheck = ValidationUtils.check(
  address -> Optional.ofNullable(address).filter(addr -> addr.contains("China")).isPresent(),
  address -> String.format("person.address should contain China, but actual is %s", address));
```

Then we compose these three validations to one for Person validation:

```java
final Validation<Person, String> personCheck = Validation.<Person, String>valid()
  .and(person -> nameCheck.validate(person.getName()))
  .and(person -> ageCheck.validate(person.getAge()))
  .and(person -> addressCheck.validate(person.getAddress()));
```

At last, the validation for person is created, which can be used as following:

```java
personCheck.validate(Person.builder().name("Jack").age(12).address("US").build()).forEach(System.out::println);
```

It will print:

```bash
Target should equal to Jack, but actual is John
person.age should be bigger than 18, but actual is 12
person.address should contain China, but actual is US
```

By using this framework, the `nameCheck`, `ageCheck`, `addressCheck` can all be reusable.

But reusability is not only limited to the created validations, you can add all common validations into ValidationUtils and combine them as needed.