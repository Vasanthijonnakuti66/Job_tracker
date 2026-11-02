package com.jobtracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested resource (Application, User, etc.) is not found.
 *
 * @ResponseStatus(NOT_FOUND) tells Spring to return HTTP 404 if this
 * exception bubbles up to the framework. However, we catch it explicitly
 * in our GlobalExceptionHandler for a more customized error response.
 *
 * WHY a custom exception instead of returning null?
 *   → Exceptions let the service layer communicate "not found" without
 *     needing the controller to check for null everywhere.
 *   → The GlobalExceptionHandler converts it to a clean 404 JSON response
 *     in one place — not scattered across every controller method.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}
