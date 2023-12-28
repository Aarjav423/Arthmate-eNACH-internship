package com.arthmate.enachapi.utils.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = ValidateRequestIdImp.class)
public @interface ValidateRequestID {
    String[] fld() default {};
    String val() default "";
    String val1() default "record not found";
    String message() default "{val} {val1}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}

