package com.arthmate.enachapi.utils.validator;

import com.arthmate.enachapi.EnachApiApplication;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.context.ConfigurableApplicationContext;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapNamePatternValidator implements ConstraintValidator<MapNamePattern, CharSequence> {
    private Map<String, String> acceptedValues;
    List<String> list ;
    private ConfigurableApplicationContext appCtx = EnachApiApplication.getCtx();

    @Override
    public void initialize(MapNamePattern annotation) {
        acceptedValues = (Map<String, String>) appCtx.getBean(annotation.globalBeanName().toString());
        list = new ArrayList<String>(acceptedValues.keySet());
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        HibernateConstraintValidatorContext hibernateContext =
                context.unwrap(HibernateConstraintValidatorContext.class);
        hibernateContext.disableDefaultConstraintViolation();
        hibernateContext
                .addMessageParameter("anyOf", acceptedValues.toString())
                .addExpressionVariable("anyOf", acceptedValues.toString())
                .buildConstraintViolationWithTemplate(hibernateContext.getDefaultConstraintMessageTemplate())
                .addConstraintViolation();
        return list.contains(value.toString());
    }
}