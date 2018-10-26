[![Build Status](https://travis-ci.org/iintothewind/validation.svg?branch=master)](https://travis-ci.org/iintothewind/validation)

# cyh.simple.Validation
A simple functional Validation framework

## Design

```Java
Iterable<E> validate(final T t);
```

## Sample

```Java
Validation<Integer, String> check1 = ValidationUtils.check((Integer i) -> i > 0, "should be greater than zero");
Validation<Integer, String> check2 = ValidationUtils.check((Integer i) -> i < 10, "should be smaller than ten");

Assertions.assertThat(check1.and(check2).validate(-1)).contains("should be greater than zero");
```