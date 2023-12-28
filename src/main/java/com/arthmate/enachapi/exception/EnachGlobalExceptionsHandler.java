package com.arthmate.enachapi.exception;

import com.arthmate.enachapi.utils.ResponseHandler;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class EnachGlobalExceptionsHandler {

    Map<String,Object> response = new HashMap<>();
    private static final String HTTP_STATUS = "HttpStatus";
    private static final String MESSAGE = "message";
    private static final String ERRORS = "errors";

    /*
      * Handler to Validate Request body
      * for missing or wrong value entered for mandatory variables
    */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
     public ResponseEntity<Object> handleMethodArgumentNotValid(HttpServletRequest request, MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        response.put(MESSAGE, "Invalid arguments passed");
        response.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
        response.put(ERRORS, errors);

        return ResponseHandler.validationResponseBuilder(response, HttpStatus.BAD_REQUEST);
    }

    /*
     * Handler to Validate  Path variable value as per applied annotation rules
     * check Path variable should not allow wrong value entered
     */

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<Object> handleConstraintViolationException(HttpServletRequest request, ConstraintViolationException ex) {
        response.put(MESSAGE, "Invalid arguments passed");
        response.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
        response.put(ERRORS, ex.getMessage());

        return ResponseHandler.validationResponseBuilder(response, HttpStatus.BAD_REQUEST);
    }

    /*
     * Handler to Validate Path Variable
     * so no path variable should be missing or wrong value entered for mandatory variables
     */

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ResponseBody
    public ResponseEntity<Object> handleRequestMethodNotSupportedException(HttpServletRequest request, HttpRequestMethodNotSupportedException ex) {
        response.put(MESSAGE, "Method not allowed");
        response.put(HTTP_STATUS, HttpStatus.METHOD_NOT_ALLOWED);
        response.put(ERRORS, new String[]{ex.getMessage(), "Verify the url", "Verify the path variable"});

        return ResponseHandler.validationResponseBuilder(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /*
     * Handler to Validate Invalid format values
     * so Validate BigDecimal, Date and other fields values should not pass any invalid text as value
     */
    @ExceptionHandler(InvalidFormatException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<Object> handleInvalidFormatException(HttpServletRequest request, InvalidFormatException ex) throws IOException {
        log.error("Error in Request Body for {}, Exception Message: {}", request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE), ex.getMessage());
        response.put(MESSAGE, "Invalid format");
        response.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
        String str =  ex.getMessage().substring(ex.getMessage().lastIndexOf("(through"));
        String fld = str.substring(str.lastIndexOf("["), str.lastIndexOf("]")+1).replaceAll("[^a-zA-Z0-9\\s-_+]","");
        response.put(ERRORS, new String[]{"Field: "+fld, ex.getMessage().replace(str,"")});

        return ResponseHandler.validationResponseBuilder(response, HttpStatus.BAD_REQUEST);
    }

    /*
     * Handler to Validate Invalid values already saved in database for multiple fields,
     * So the record unable to read or load in entity.
     * Ex. A String can not load on Long or BigDecimal or invalid value assigned to a Enum property
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ResponseEntity<Object> handleIllegalArgumentException(HttpServletRequest request, IllegalArgumentException ex) {
        log.error("Error in Record to load in Entity for {}, Exception Message: {}", request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE), ex.getMessage());
        response.put(MESSAGE, "Unable to read, load the record, one or more fields may have invalid values assigned");
        response.put(HTTP_STATUS, HttpStatus.INTERNAL_SERVER_ERROR);
        response.put(ERRORS, new String[]{ ex.getMessage().replace("com.arthmate.enachapi.","")});

        return ResponseHandler.validationResponseBuilder(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
