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
  static <T, E> Validation<T, E> check(final Predicate<T> predicate, final E error) {
    Objects.requireNonNull(predicate, "predicate is required");
    Objects.requireNonNull(error, "error message is required");
    return (T input) -> {
      if (predicate.test(input)) {
        return Collections.emptyList();
      } else {
        return Collections.singletonList(error);
      }
    };
  }

  static <T, E> Validation<T, E> check(final Predicate<T> predicate, final Function<T, E> function) {
    Objects.requireNonNull(predicate, "predicate is required");
    Objects.requireNonNull(function, "error message is required");
    return (T input) -> {
      if (predicate.test(input)) {
        return Collections.emptyList();
      } else {
        return Collections.singletonList(function.apply(input));
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

  static <T> Validation<T, String> checkNonNull() {
    return check(Objects::nonNull, "target is required not null");
  }

  static <T> Validation<T, String> checkEqual(final T expected) {
    return check(actual -> Objects.equals(expected, actual), target -> String.format("target should equal to %s, but actual is %s", target, expected));
  }

  static Validation<String, String> checkStringNotEmpty(final String error) {
    return check(input -> Optional.ofNullable(input).filter(s -> !s.isEmpty()).isPresent(), error);
  }

}
