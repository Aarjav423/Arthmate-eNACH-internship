package com.arthmate.enachapi.utils.validator;

import com.arthmate.enachapi.EnachApiApplication;
import com.arthmate.enachapi.model.EnachDetail;
import com.arthmate.enachapi.service.EnachDetailsService;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@RequiredArgsConstructor
public class ValidateRequestIdImp implements ConstraintValidator<ValidateRequestID, CharSequence> {

    private final EnachDetailsService enachDetailsService;

    private EnachDetail enachDetail = null;

    @Override
    public void initialize(ValidateRequestID annotation) {
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        enachDetail = enachDetailsService.getEnachDetailByRequest(value.toString());
        if( enachDetail != null && StringUtils.hasText(enachDetail.getReferenceNumber()) == false){
            return true;
        }

        HibernateConstraintValidatorContext hibernateContext =
                context.unwrap(HibernateConstraintValidatorContext.class);
        hibernateContext.disableDefaultConstraintViolation();
        hibernateContext
                .addMessageParameter("val", value)
                .addExpressionVariable("val", value);
        if( enachDetail != null && StringUtils.hasText(enachDetail.getReferenceNumber()) == true){
            hibernateContext
                    .addMessageParameter("val1", "is not permitted for any change")
                    .addExpressionVariable("val1", "is not permitted for any change");
        }
        hibernateContext.buildConstraintViolationWithTemplate(hibernateContext.getDefaultConstraintMessageTemplate())
                .addConstraintViolation();

        return false;
    }
}