package com.arthmate.enachapi.utils.validator;

import com.arthmate.enachapi.model.EnachDetail;
import com.arthmate.enachapi.service.EnachDetailsService;
import com.arthmate.enachapi.utils.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ActionPermissionMatrixByAuthImp implements ConstraintValidator<ActionPermissionMatrixByAuth, Authentication> {

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
    public void initialize(ActionPermissionMatrixByAuth annotation) {
        cnd =  annotation.cnd();
        act =  annotation.act();
        val =  annotation.val();
    }

    @Override
    public boolean isValid(Authentication value, ConstraintValidatorContext context) {
        String request_id = jwtTokenUtil.getRequestIdByAuthenticationObj(value).toString();
        enachDetail = enachDetailsService.getEnachDetailByRequest(request_id);
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
      //  if( enachDetail != null  &&  Arrays.stream(stats).anyMatch(enachDetail.getStatus()::equals) == true){
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