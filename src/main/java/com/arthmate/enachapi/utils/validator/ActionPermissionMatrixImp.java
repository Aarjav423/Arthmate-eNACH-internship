package com.arthmate.enachapi.utils.validator;

import com.arthmate.enachapi.model.EnachDetail;
import com.arthmate.enachapi.service.EnachDetailsService;
import com.arthmate.enachapi.utils.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ActionPermissionMatrixImp implements ConstraintValidator<ActionPermissionMatrix, CharSequence> {

    private final EnachDetailsService enachDetailsService;

    private final JwtTokenUtil jwtTokenUtil;
    private EnachDetail enachDetail = null;
    String cnd = "";
    String act = "";
    String val = "";
    List<String> stats = new ArrayList<>();

    @Value("#{${enach.action.permission.matrix}}")
    Map<String, List<String>> permissionMatrix;

    @Override
    public void initialize(ActionPermissionMatrix annotation) {
        cnd =  annotation.cnd();
        act =  annotation.act();
        val =  annotation.val();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        enachDetail = enachDetailsService.getEnachDetailByRequest(value.toString());
        stats = permissionMatrix.get(cnd);

        if( enachDetail != null  &&  stats.stream().anyMatch(enachDetail.getStatus()::equals) == true){
            return true;
        }

        HibernateConstraintValidatorContext hibernateContext =
                context.unwrap(HibernateConstraintValidatorContext.class);
        hibernateContext.disableDefaultConstraintViolation();
        hibernateContext
                .addMessageParameter("val", val+ " for \"Action: "+act +"\" because of invalid status")
                .addExpressionVariable("val", val+ " for \"Action: "+act+ "\" because of invalid status");
        if( enachDetail != null  &&  stats.stream().anyMatch(enachDetail.getStatus()::equals) == false){
            hibernateContext
                    .addMessageParameter("val1", "Valid status required: "+ stats.toString() + " \"Found Status: "+ enachDetail.getStatus()+"\"")
                    .addExpressionVariable("val1", "Valid status required: "+ stats.toString() + " \"Found Status: "+ enachDetail.getStatus()+"\"");

        }
        hibernateContext.buildConstraintViolationWithTemplate(hibernateContext.getDefaultConstraintMessageTemplate())
                .addConstraintViolation();

        return false;
    }
}