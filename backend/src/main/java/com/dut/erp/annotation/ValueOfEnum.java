package com.dut.erp.annotation;

import com.dut.erp.validator.ValueOfEnumValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Constraint(validatedBy = ValueOfEnumValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValueOfEnum {
  Class<? extends Enum<?>> enumClass();

  String message() default "must be any of enum {enumValues}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
