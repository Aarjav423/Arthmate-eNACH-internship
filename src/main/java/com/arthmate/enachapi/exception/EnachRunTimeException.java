package com.arthmate.enachapi.exception;


public class EnachRunTimeException extends RuntimeException {
    public EnachRunTimeException(String msg) {
        super(msg);
    }

    public EnachRunTimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

