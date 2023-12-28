package com.arthmate.enachapi.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ResponseHandler {
    public static ResponseEntity<Object> responseBuilder(String message, HttpStatus httpStatus, Object responseObject){
        Map<String,Object> response = new HashMap<>();
        response.put("message", message);
        response.put("HttpStatus", httpStatus);
        response.put("data", responseObject);

        return new ResponseEntity<>(response,httpStatus);
    }
    public static ResponseEntity<Object> validationResponseBuilder(Map<String,Object> response, HttpStatus httpStatus){
        return new ResponseEntity<>(response,httpStatus);
    }
}
