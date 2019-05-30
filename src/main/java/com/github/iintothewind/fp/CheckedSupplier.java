package com.github.iintothewind.fp;

@FunctionalInterface
public interface CheckedSupplier<T> {
  T get() throws Throwable;
}
