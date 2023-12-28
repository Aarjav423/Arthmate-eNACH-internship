package com.arthmate.enachapi.utils.validator;

import com.arthmate.enachapi.EnachApiApplication;
import com.arthmate.enachapi.model.LiveBankStatusResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.context.ConfigurableApplicationContext;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

@Slf4j
public class BankDetailsValidator implements ConstraintValidator<BankDetails, Object> {

    private String bankFieldName;
    private String authenticationModeFieldName;
    private Map<String, LiveBankStatusResponseBody> bankList;
    private ConfigurableApplicationContext appCtx = EnachApiApplication.getCtx();

    @Override
    public void initialize(BankDetails annotation) {
        bankFieldName = annotation.bankFieldName();
        authenticationModeFieldName = annotation.authenticationModeFieldName();
        bankList = (Map<String, LiveBankStatusResponseBody>) appCtx.getBean(annotation.globalBeanName().toString());
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        String message = "";
        try {
            String bank = BeanUtils.getProperty(value, bankFieldName);
            String authenticationMode = BeanUtils.getProperty(value, authenticationModeFieldName);
            if (bank == null) {
                return true;
            }
            if (bankList.containsKey(bank)) {
                List<String> body = bankList.get(bank).getAccessMode();
                if (body.isEmpty() || body.contains(authenticationMode)) {
                    return true;
                } else {
                    message = String.format("%s authentication mode for %s bank is invalid", authenticationMode, bank);
                }
            } else {
                message = String.format("%s bank name is invalid", bank);
            }
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("bank").addConstraintViolation();
            return false;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            log.error("error in validation bank and authentication mode ", ex);
        }
        return false;
    }
}
