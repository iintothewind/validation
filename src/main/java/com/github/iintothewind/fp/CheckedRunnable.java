package com.github.iintothewind.fp;

@FunctionalInterface
public interface CheckedRunnable {
    void run() throws Throwable;
}
