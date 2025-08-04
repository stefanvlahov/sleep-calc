package org.svlahov.sleepcalc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, String> errorBody = Map.of(
                "status", "400",
                "error", "Bad Request",
                "message", ex.getMessage()
        );
        return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
    }
}
