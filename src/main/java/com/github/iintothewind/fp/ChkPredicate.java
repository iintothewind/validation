package com.github.iintothewind.fp;

import java.util.Objects;


@FunctionalInterface
public interface ChkPredicate<T> {
  static <T> ChkPredicate<T> isEqual(Object that) {
    return (null == that) ? Objects::isNull : that::equals;
  }

  boolean test(T t) throws Throwable;

  default ChkPredicate<T> negate() {
    return t -> !test(t);
  }

  default ChkPredicate<T> and(ChkPredicate<? super T> other) {
    Objects.requireNonNull(other);
    return (t) -> test(t) && other.test(t);
  }

  default ChkPredicate<T> or(ChkPredicate<? super T> other) {
    Objects.requireNonNull(other);
    return (t) -> test(t) || other.test(t);
  }

  static <T> ChkPredicate<T> nonNull() {
    return Objects::isNull;
  }
}
