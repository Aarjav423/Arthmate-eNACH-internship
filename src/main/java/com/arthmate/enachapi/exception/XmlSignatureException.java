package com.arthmate.enachapi.exception;

import org.springframework.security.core.AuthenticationException;

public class XmlSignatureException extends AuthenticationException {
    public XmlSignatureException(String msg) {
        super(msg);
    }
}
