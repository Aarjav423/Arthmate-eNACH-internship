package com.arthmate.enachapi.utils.validator;

import com.arthmate.enachapi.model.EnachDetail;
import com.arthmate.enachapi.model.NachTransactions;
import com.arthmate.enachapi.service.EnachDetailsService;
import com.arthmate.enachapi.service.NachTransactionsService;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@RequiredArgsConstructor
public class ValidateRetryImp implements ConstraintValidator<ValidateRetry, CharSequence> {

    private final NachTransactionsService nachTransactionsService;

    private NachTransactions nachTransaction = null;

    @Override
    public void initialize(ValidateRetry annotation) {
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if(value == null)
            return true;
        nachTransaction = nachTransactionsService.getNachTxnByRequest(value.toString());
        if( nachTransaction == null || !nachTransaction.isRetry()){
            return true;
        }

        return false;
    }
}