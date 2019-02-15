package cyh.simple;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


@FunctionalInterface
public interface Validation<T, E> {
  static <T, E> Validation<T, E> valid() {
    return (T t) -> Collections.emptyList();
  }

  Iterable<E> validate(final T t);

  default Validation<T, E> and(final Validation<? super T, ? extends E> other) {
    return (T t) -> Optional
      .ofNullable(other)
      .filter(o -> Optional.ofNullable(o.validate(t)).map(Iterable::iterator).filter(Iterator::hasNext).isPresent())
      .map(that -> Stream
        .concat(
          StreamSupport.stream(Optional
            .ofNullable(validate(t))
            .map(Iterable::spliterator)
            .orElse(Collections.<E>emptyList().spliterator()), true),
          StreamSupport.stream(that.validate(t).spliterator(), true))
        .collect(Collectors.toList()))
      .orElse(StreamSupport
        .stream(Optional
          .ofNullable(validate(t)).map(Iterable::spliterator)
          .orElse(Collections.<E>emptyList().spliterator()), true)
        .collect(Collectors.toList()));
  }

  default Validation<T, E> or(final Validation<? super T, ? extends E> other) {
    return (T t) -> {
      if (Optional.ofNullable(validate(t)).filter(iterable -> iterable.iterator().hasNext()).isPresent()
        && Optional.ofNullable(other).map(o -> o.validate(t)).filter(iterable -> iterable.iterator().hasNext()).isPresent()) {
        return Stream
          .concat(StreamSupport.stream(validate(t).spliterator(), true), StreamSupport.stream(other.validate(t).spliterator(), true))
          .collect(Collectors.toList());
      } else {
        return Collections.emptyList();
      }
    };
  }
}
