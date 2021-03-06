package com.github.iintothewind;

import java.util.Objects;
import java.util.function.Predicate;


public interface MorePredicates {
  static <T> Predicate<T> nonNull() {
    return Objects::nonNull;
  }

}
