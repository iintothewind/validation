package com.github.iintothewind.fp;

import java.util.Objects;


@FunctionalInterface
public interface CheckedPredicate<T> {
  static <T> CheckedPredicate<T> isEqual(Object that) {
    return (null == that) ? Objects::isNull : that::equals;
  }

  boolean test(T t) throws Throwable;

  default CheckedPredicate<T> negate() {
    return t -> !test(t);
  }

  default CheckedPredicate<T> and(CheckedPredicate<? super T> other) {
    Objects.requireNonNull(other);
    return (t) -> test(t) && other.test(t);
  }

  default CheckedPredicate<T> or(CheckedPredicate<? super T> other) {
    Objects.requireNonNull(other);
    return (t) -> test(t) || other.test(t);
  }
}
