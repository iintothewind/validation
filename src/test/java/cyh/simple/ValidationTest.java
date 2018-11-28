package cyh.simple;

import com.google.common.collect.ImmutableList;
import javaslang.control.Try;
import org.assertj.core.api.Assertions;
import org.junit.Test;


public class ValidationTest {
  @Test
  public void testValidateVargs() {
    Validation<Integer, String> check = ValidationUtils.check((Integer i) -> i > 0, "should be greater than zero");
    Assertions.assertThat(check.validate(-1)).contains("should be greater than zero");
  }

  @Test
  public void testAnd() {
    Validation<Integer, String> check1 = ValidationUtils.check((Integer i) -> i > 0, "should be greater than zero");
    Validation<Integer, String> check2 = ValidationUtils.check((Integer i) -> i < 10, "should be smaller than ten");

    Assertions.assertThat(check1.and(check2).validate(-1)).contains("should be greater than zero");
    Assertions.assertThat(check1.and(check2).validate(11)).contains("should be smaller than ten");

  }

  @Test
  public void testOr() {
    Validation<Integer, String> check1 = ValidationUtils.check((Integer i) -> i < 0, "should be greater than zero");
    Validation<Integer, String> check2 = ValidationUtils.check((Integer i) -> i > 10, "should be smaller than ten");

    Assertions.assertThat(check1.or(check2).validate(-1)).isEmpty();
    Assertions.assertThat(check1.or(check2).validate(11)).isEmpty();
  }

  @Test
  public void testCheckNotEmpty() {
    Validation<String, String> checkStr = ValidationUtils.checkStringNotEmpty("input is required");
    checkStr.validate(null).forEach(System.out::println);
  }

  @Test
  public void testCheckEquals() {
    Validation<String, String> checkEq = ValidationUtils.checkEqual("abc");
    checkEq.validate("asd").forEach(System.out::println);
    Validation<Try<String>, String> checkTs = ValidationUtils.check((Try<String> t) -> t.filter(s -> s.startsWith("star")).isSuccess(), "input should start with star");
    checkTs.validate(Try.success("sss")).forEach(System.out::println);
  }

  @Test
  public void testCheckWithErrorFunction() {
    Validation<String, String> checkStartWith = ValidationUtils.check(s -> s.startsWith("sss"), s -> String.format("%s should start with sss", s));
    Assertions.assertThat(checkStartWith.validate("abc")).contains("abc should start with sss");
  }

  @Test
  public void testCheckAll() {
    final Validation<Iterable<Integer>, String> checkAllInts = ValidationUtils.checkAll(ValidationUtils.check(i -> i > 0, i -> String.format("%s should be bigger than 0", i)));
    final Iterable<String> errors = checkAllInts.validate(ImmutableList.of(-1, 0, 1, 2, 3));
    Assertions.assertThat(errors).contains("-1 should be bigger than 0", "0 should be bigger than 0");
  }
}
