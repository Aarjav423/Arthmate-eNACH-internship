package com.arthmate.enachapi.utils.validator;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ConsentTimestampValidator implements ConstraintValidator<ConsentTimestamp, String> {

    @Override
    public void initialize(ConsentTimestamp constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String consentTimestamp, ConstraintValidatorContext context) {
        if (consentTimestamp == null){
            addConstraintViolation(context, "consent_timestamp is required.");
            return false;
        } else if(!isValidTimestampFormat(consentTimestamp)){
            addConstraintViolation(context, "consent_timestamp is in an invalid format");
            return false;
        }

        try {
            LocalDateTime parsedTimestamp = parseConsentTimestamp(consentTimestamp);
            LocalDateTime currentDateTime = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));

            if (parsedTimestamp.isAfter(currentDateTime)) {
                addConstraintViolation(context, "consent_timestamp should be less than or equal to current date and time.");
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private boolean isValidTimestampFormat(String consentTimestamp) {
        return consentTimestamp.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}[+-]\\d{2}:\\d{2}$");
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        HibernateConstraintValidatorContext hibernateContext =
                context.unwrap(HibernateConstraintValidatorContext.class);
        hibernateContext.disableDefaultConstraintViolation();
        hibernateContext
                .addMessageParameter("message", message)
                .addExpressionVariable("message", message)
                .buildConstraintViolationWithTemplate(hibernateContext.getDefaultConstraintMessageTemplate())
                .addConstraintViolation();
    }

    private LocalDateTime parseConsentTimestamp(String consentTimestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        return LocalDateTime.parse(consentTimestamp, formatter);
    }
}
