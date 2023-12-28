package com.arthmate.enachapi.utils.validator;

import com.arthmate.enachapi.dto.EnachDtlRqstBdy;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EndDateNotBeforeStartDateValidator implements ConstraintValidator<EndDateNotBeforeStartDate, EnachDtlRqstBdy> {

    String endDateField;

    @Override
    public void initialize(EndDateNotBeforeStartDate constraintAnnotation) {
        endDateField = constraintAnnotation.endDateField();
    }

    @Override
    public boolean isValid(EnachDtlRqstBdy value, ConstraintValidatorContext ctx) {
        try {
            if (value.getEndDate()==null || value.getStartDate()==null) {
                return true;
            }

            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(ctx.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(endDateField)
                    .addConstraintViolation();

            return !value.getEndDate().isBefore(value.getStartDate());
        } catch (Exception e) {
            return false;
        }

    }
}
