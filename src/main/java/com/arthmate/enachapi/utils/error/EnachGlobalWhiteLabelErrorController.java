package com.arthmate.enachapi.utils.error;

import com.arthmate.enachapi.utils.ResponseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
public class EnachGlobalWhiteLabelErrorController implements ErrorController {
    @Autowired private ErrorAttributes errorAttributes;

    Map<String,Object> response = new HashMap<>();
    private static final String HTTP_STATUS = "HttpStatus";
    private static final String MESSAGE = "message";
    private static final String ERRORS = "errors";
    private static final String ERROR_Message = "errorMessage";
    private static final String PATH = "path";
    private static final String TRACE = "trace";

    ErrorAttributeOptions options = ErrorAttributeOptions
            .defaults()
            .including(ErrorAttributeOptions.Include.MESSAGE)
            ;

    public void setErrorAttributes(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping(value = "error")
    @ResponseBody
    public ResponseEntity<Object> error(WebRequest webRequest, HttpServletResponse res) {
        Map<String, Object> errorAttributes = getErrorAttributes(webRequest);
        response.put(MESSAGE, (String) errorAttributes.get("message"));
        response.put(HTTP_STATUS, (String) (res.getStatus() + " "+ errorAttributes.get("error")));
        response.put(ERRORS, new HashMap<String, String>() {{
            put(PATH, (String) errorAttributes.get("path"));
            put(ERROR_Message, (String) errorAttributes.get("message"));
            put(TRACE, (String) errorAttributes.get("trace"));
        }});

        return ResponseHandler.validationResponseBuilder(response, HttpStatus.valueOf(res.getStatus()));
    }

    public String getErrorPath() {
        return "error";
    }

    private Map<String, Object> getErrorAttributes(WebRequest webRequest) {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.putAll(errorAttributes.getErrorAttributes(webRequest, options));
        return errorMap;
    }
}