package com.github.iintothewind;

import com.google.common.collect.Lists;
import javaslang.control.Try;
import lombok.Builder;
import lombok.Getter;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.Optional;


public class ValidationTest {
  @Test
  public void testValidateVargs() {
    final Validation<Integer, String> check = ValidationUtils.check((Integer target) -> Optional.ofNullable(target).filter(i -> i > 0).isPresent(), "should be greater than zero");
    Assertions.assertThat(check.validate(-1)).contains("should be greater than zero");
  }

  @Test
  public void testAnd() {
    final Validation<Integer, String> nullCheck = ValidationUtils.checkNonNull();
    final Validation<Integer, String> check1 = ValidationUtils.check((Integer target) -> Optional.ofNullable(target).filter(i -> i > 0).isPresent(), "should be greater than zero");
    final Validation<Integer, String> check2 = ValidationUtils.check((Integer target) -> Optional.ofNullable(target).filter(i -> i < 10).isPresent(), "should be smaller than ten");
    final Validation<Integer, String> numberCheck = nullCheck.and(check1).and(check2);
    Assertions.assertThat(numberCheck.validate(null)).contains("target is required not null", "should be greater than zero", "should be smaller than ten");
    Assertions.assertThat(numberCheck.validate(-1)).contains("should be greater than zero");
    Assertions.assertThat(numberCheck.validate(11)).contains("should be smaller than ten");

  }

  @Test
  public void testOr() {
    final Validation<Integer, String> nullCheck = ValidationUtils.checkNonNull();
    final Validation<Integer, String> check1 = ValidationUtils.check((Integer target) -> Optional.ofNullable(target).filter(i -> i < 0).isPresent(), "should be greater than zero");
    final Validation<Integer, String> check2 = ValidationUtils.check((Integer target) -> Optional.ofNullable(target).filter(i -> i > 10).isPresent(), "should be smaller than ten");
    final Validation<Integer, String> numberCheck = nullCheck.and(check1.or(check2));
    Assertions.assertThat(numberCheck.validate(-1)).isEmpty();
    Assertions.assertThat(numberCheck.validate(11)).isEmpty();
    Assertions.assertThat(numberCheck.validate(null)).contains("target is required not null", "should be greater than zero", "should be smaller than ten");
  }

  @Test
  public void testCheckNotEmpty() {
    final Validation<String, String> checkStr = ValidationUtils.checkStringNotEmpty("input is required");
    checkStr.validate(null).forEach(System.out::println);
  }

  @Test
  public void testCheckEquals() {
    final Validation<String, String> checkEq = ValidationUtils.checkEqual("abc");
    checkEq.validate("asd").forEach(System.out::println);
    Validation<Try<String>, String> checkTs = ValidationUtils
      .check((Try<String> t) -> Optional
          .ofNullable(t)
          .orElse(Try.failure(new NullPointerException("input Try is null")))
          .filter(s -> s.startsWith("star")).isSuccess(),
        input -> String.format("input %s should start with star", input));
    checkTs.validate(null).forEach(System.out::println);
    checkTs.validate(Try.success("sss")).forEach(System.out::println);
  }

  @Test
  public void testCheckWithErrorFunction() {
    Validation<String, String> checkStartWith = ValidationUtils.check(
      target -> Optional.ofNullable(target).filter(s -> s.startsWith("sss")).isPresent(),
      s -> String.format("%s should start with sss", s));
    Assertions.assertThat(checkStartWith.validate("abc")).contains("abc should start with sss");
  }

  @Test
  public void testCheckAll() {
    final Validation<Iterable<Integer>, String> checkAllInts = ValidationUtils.checkAll(ValidationUtils.check(
      target -> Optional.ofNullable(target).filter(i -> i > 0).isPresent(),
      i -> String.format("%s should be bigger than 0", i)));
    final Iterable<String> errors = checkAllInts.validate(Lists.newArrayList(-1, 0, 1, 2, 3, null));
    Assertions.assertThat(errors).contains("-1 should be bigger than 0", "0 should be bigger than 0", "null should be bigger than 0");
  }

  @Test
  public void testCheckPerson() {

    final Validation<String, String> nameCheck = ValidationUtils.checkEqual("John");

    final Validation<Integer, String> ageCheck = ValidationUtils.check(
      age -> Optional.ofNullable(age).filter(i -> i > 18).isPresent(),
      age -> String.format("person.age should be bigger than 18, but actual is %s", age));

    final Validation<String, String> addressCheck = ValidationUtils.check(
      address -> Optional.ofNullable(address).filter(addr -> addr.contains("China")).isPresent(),
      address -> String.format("person.address should contain China, but actual is %s", address));

    final Validation<Person, String> personCheck = Validation.<Person, String>valid()
      .and(person -> nameCheck.validate(person.getName()))
      .and(person -> ageCheck.validate(person.getAge()))
      .and(person -> addressCheck.validate(person.getAddress()));

    personCheck.validate(Person.builder().name("Jack").age(12).address("US").build()).forEach(System.out::println);
  }

  @Getter
  @Builder
  public static class Person {
    private String name;
    private int age;
    private String address;
  }
}
