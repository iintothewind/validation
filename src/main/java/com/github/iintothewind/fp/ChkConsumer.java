package com.github.iintothewind.fp;

import java.util.Objects;


@FunctionalInterface
public interface ChkConsumer<T> {
  void accept(T value) throws Throwable;

  default ChkConsumer<T> andThen(ChkConsumer<? super T> after) {
    Objects.requireNonNull(after);
    return (T t) -> {
      accept(t);
      after.accept(t);
    };
  }
}
