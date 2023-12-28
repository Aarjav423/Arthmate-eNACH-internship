package com.arthmate.enachapi.exception;

import org.springframework.security.core.AuthenticationException;

import javax.validation.ConstraintViolationException;

public class NachTransactionNotFoundException extends ConstraintViolationException {
    public NachTransactionNotFoundException(String msg) {
        super(msg, null);
    }

//    public NachTransactionNotFoundException(String msg, Throwable cause) {
//        super(msg, cause);
//    }
}

