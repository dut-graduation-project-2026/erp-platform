package com.dut.erp.validator;

import com.dut.erp.annotation.ValueOfEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ValueOfEnumValidator implements ConstraintValidator<ValueOfEnum, String> {

  private Set<String> validValues;

  @Override
  public void initialize(ValueOfEnum annotation) {
    validValues =
        Arrays.stream(annotation.enumClass().getEnumConstants())
            .map(Enum::name)
            .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    // Let @NotBlank handle null/empty
    if (value == null || value.isBlank()) {
      return true;
    }

    boolean valid = validValues.contains(value);

    if (!valid) {
      context.disableDefaultConstraintViolation();

      context
          .buildConstraintViolationWithTemplate(
              context
                  .getDefaultConstraintMessageTemplate()
                  .replace("{enumValues}", String.join(", ", validValues)))
          .addConstraintViolation();
    }

    return valid;
  }
}
