package com.github.iintothewind;


import com.google.common.collect.Lists;
import io.vavr.control.Try;
import lombok.Builder;
import lombok.Getter;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.Optional;


public class ValidationTest {
  @Test
  public void testValidateVargs() {
    final Validation<Integer, String> check = ValidationUtils.checkInteger(i -> i > 0, actual -> String.format("%s should be greater than zero", actual));
    Assertions.assertThat(check.validate(null)).contains("null should be greater than zero");
    Assertions.assertThat(check.validate(-1)).contains("-1 should be greater than zero");
  }

  @Test
  public void testAnd() {
    final Validation<Integer, String> check1 = ValidationUtils.checkInteger(i -> i > 0, actual -> String.format("%s should be greater than zero", actual));
    final Validation<Integer, String> check2 = ValidationUtils.checkInteger(i -> i < 10, actual -> String.format("%s should be smaller than ten", actual));
    final Validation<Integer, String> numberCheck = check1.and(check2);
    Assertions.assertThat(numberCheck.validate(null)).contains("null should be greater than zero", "null should be smaller than ten");
    Assertions.assertThat(numberCheck.validate(-1)).contains("-1 should be greater than zero");
    Assertions.assertThat(numberCheck.validate(11)).contains("11 should be smaller than ten");
  }

  @Test
  public void testOr() {
    final Validation<Integer, String> check1 = ValidationUtils.checkInteger(i -> i < 0, actual -> String.format("%s should be smaller than zero", actual));
    final Validation<Integer, String> check2 = ValidationUtils.checkInteger(i -> i > 10, actual -> String.format("%s should be smaller than ten", actual));
    final Validation<Integer, String> numberCheck = check1.or(check2);
    Assertions.assertThat(numberCheck.validate(-1)).isEmpty();
    Assertions.assertThat(numberCheck.validate(11)).isEmpty();
    Assertions.assertThat(numberCheck.validate(null)).contains("null should be smaller than zero", "null should be smaller than ten");
  }

  @Test
  public void testCheckNotEmpty() {
    final Validation<String, String> checkStr = ValidationUtils.checkStringNotEmpty();
    Assertions.assertThat(checkStr.validate(null)).contains("target string should not be empty");
  }

  @Test
  public void testCheckEquals() {
    final Validation<String, String> checkEq = ValidationUtils.checkEqual("abc");
    Assertions.assertThat(checkEq.validate("asd")).contains("target should equal to abc, but actual is asd");
  }

  @Test
  public void testCheckAll() {
    final Validation<Iterable<Integer>, String> checkAllInts = ValidationUtils.checkAll(ValidationUtils.checkInteger(
      i -> i > 0,
      i -> String.format("%s should be bigger than 0", i)));
    final Iterable<String> errors = checkAllInts.validate(Lists.newArrayList(-1, 0, 1, 2, 3, null));
    Assertions.assertThat(errors).contains("-1 should be bigger than 0", "0 should be bigger than 0", "null should be bigger than 0");
  }

  @Test
  public void testSafeCheck() {
    Validation<Try<String>, String> checkNs = ValidationUtils.check(
      Predicables.<Try<String>>nonNull().and(t -> t.mapTry(Integer::parseInt).isSuccess()),
      input -> String.format("input %s should be a number string", input));
    Assertions.assertThat(checkNs.validate(null)).contains("input null should be a number string");
    Assertions.assertThat(checkNs.validate(Try.success("sss"))).contains("input Success(sss) should be a number string");
  }

  @Test
  public void testCheckPerson() {
    final Validation<String, String> nameCheck = ValidationUtils.checkString(
      name -> name.equals("John"),
      name -> String.format("person.name should be equal to John, but actual is %s", name));

    final Validation<Integer, String> ageCheck = ValidationUtils.checkInteger(
      age -> age > 18,
      age -> String.format("person.age should be bigger than 18, but actual is %s", age));

    final Validation<String, String> addressCheck = ValidationUtils.checkString(
      address -> address.contains("China"),
      address -> String.format("person.address should contain China, but actual is %s", address));

    final Validation<Person, String> personCheck = Validation.<Person, String>valid()
      .and(person -> nameCheck.validate(Optional.ofNullable(person).map(Person::getName).orElse("")))
      .and(person -> ageCheck.validate(Optional.ofNullable(person).map(Person::getAge).orElse(0)))
      .and(person -> addressCheck.validate(Optional.ofNullable(person).map(Person::getAddress).orElse("")));

    final Iterable<String> result = personCheck.validate(Person.builder().name("Jack").age(12).address("US").build());
    Assertions.assertThat(result).contains(
      "person.name should be equal to John, but actual is Jack",
      "person.age should be bigger than 18, but actual is 12",
      "person.address should contain China, but actual is US"
    );
  }

  @Test
  public void testSafeCheckPerson() {
    final Validation<Try<String>, String> nameCheck = ValidationUtils.check(
      Predicables.<Try<String>>nonNull().and(t -> t.filterTry("John"::equals).isSuccess()),
      t -> t
        .map(s -> String.format("person.name should be equal to John, but actual is %s", s))
        .recover(throwable -> String.format("person.name: %s", throwable))
        .getOrElse("person.name: validation error"));

    final Validation<Try<Integer>, String> ageCheck = ValidationUtils.check(
      Predicables.<Try<Integer>>nonNull().and(t -> t.filterTry(i -> i > 18).isSuccess()),
      t -> t
        .map(s -> String.format("person.age should be bigger than 18, but actual is %s", s))
        .recover(throwable -> String.format("person.age: %s", throwable))
        .getOrElse("person.age: validation error"));

    final Validation<Try<String>, String> addressCheck = ValidationUtils.check(
      Predicables.<Try<String>>nonNull().and(t -> t.filterTry(addr -> addr.contains("China")).isSuccess()),
      t -> t
        .map(s -> String.format("person.address should contain China, but actual is %s", s))
        .recover(throwable -> String.format("person.address: %s", throwable))
        .getOrElse("person.address: validation error"));

    final Validation<Person, String> personCheck = Validation.<Person, String>valid()
      .and(person -> nameCheck.validate(Try.of(() -> person.getName())))
      .and(person -> ageCheck.validate(Try.of(() -> person.getAge())))
      .and(person -> addressCheck.validate(Try.of(() -> person.getAddress())));

    personCheck.validate(null).forEach(s -> System.out.println(s));
  }

  @Getter
  @Builder
  public static class Person {
    private String name;
    private int age;
    private String address;
  }
}
