package com.arthmate.enachapi.utils.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = EndDateNotBeforeStartDateValidator.class)
public @interface EndDateNotBeforeStartDate {
    String message() default "end_date cannot be before start_date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String endDateField() default "endDate";
}
