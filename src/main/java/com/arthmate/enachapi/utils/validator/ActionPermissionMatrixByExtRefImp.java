package com.arthmate.enachapi.utils.validator;

import com.arthmate.enachapi.model.EnachDetail;
import com.arthmate.enachapi.service.EnachDetailsService;
import com.arthmate.enachapi.utils.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ActionPermissionMatrixByExtRefImp implements ConstraintValidator<ActionPermissionMatrixByExtRef, CharSequence> {

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
    public void initialize(ActionPermissionMatrixByExtRef annotation) {
        cnd =  annotation.cnd();
        act =  annotation.act();
        val =  annotation.val();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if( value == null){
            return true;
        }
        stats = permissionMatrix.get(cnd);
        var recCnt = enachDetailsService.getEnachCountByExtRefNum(value.toString(), stats);
        if( recCnt == 0){
            return true;
        }
        return false;
    }
}