package com.github.iintothewind.fp;

import java.util.Objects;


@FunctionalInterface
public interface ChkFunction<T, R> {
  static <T> ChkFunction<T, T> identity() {
    return t -> t;
  }

  R apply(T t) throws Throwable;

  default <V> ChkFunction<V, R> compose(ChkFunction<? super V, ? extends T> before) {
    Objects.requireNonNull(before);
    return (V v) -> apply(before.apply(v));
  }

  default <V> ChkFunction<T, V> andThen(ChkFunction<? super R, ? extends V> after) {
    Objects.requireNonNull(after);
    return (T t) -> after.apply(apply(t));
  }
}
