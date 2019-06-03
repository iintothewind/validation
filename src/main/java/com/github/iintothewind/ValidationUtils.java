package com.github.iintothewind;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public interface ValidationUtils {
  static <T, E> Validation<T, E> check(final Predicate<T> predicate, final Function<T, E> errorFunction) {
    Objects.requireNonNull(predicate, "predicate is required");
    Objects.requireNonNull(errorFunction, "errorFunction is required");
    return (T input) -> {
      if (predicate.test(input)) {
        return Collections.emptyList();
      } else {
        return Collections.singletonList(errorFunction.apply(input));
      }
    };
  }

  static <T> Validation<Iterable<T>, String> checkAll(final Validation<T, String> v) {
    return (Iterable<T> iterable) -> StreamSupport.stream(iterable.spliterator(), true)
      .flatMap(t -> {
        try {
          return StreamSupport.stream(v.validate(t).spliterator(), true);
        } catch (Throwable throwable) {
          return Stream.of(String.format("got error %s when validate %s", throwable.getMessage(), t));
        }
      }).distinct().collect(Collectors.toList());
  }

  static <T, E> Validation<T, E> checkNonNull(final Function<T, E> errorFunction) {
    return check(Objects::nonNull, errorFunction);
  }

  static <T> Validation<T, String> checkNonNull() {
    return checkNonNull(actual -> "target should not be null");
  }

  static <T, E> Validation<T, E> checkEqual(final T expected, final Function<T, E> errorFunction) {
    return check(actual -> Objects.equals(expected, actual), errorFunction);
  }

  static <T> Validation<T, String> checkEqual(final T expected) {
    return checkEqual(expected, actual -> String.format("target should equal to %s, but actual is %s", expected, actual));
  }

  static <E> Validation<String, E> checkStringNotEmpty(final Function<String, E> errorFunction) {
    return check(input -> Optional.ofNullable(input).filter(s -> !s.isEmpty()).isPresent(), errorFunction);
  }

  static Validation<String, String> checkStringNotEmpty() {
    return checkStringNotEmpty(actual -> "target string should not be empty");
  }
}
