package com.arthmate.enachapi.exception;

import org.springframework.security.core.AuthenticationException;

public class EnachDetailsNotFoundException extends AuthenticationException {
    public EnachDetailsNotFoundException(String msg) {
        super(msg);
    }

    public EnachDetailsNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

