package com.jobtracker.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtracker.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Writes our standard ErrorResponse JSON from the security filter chain.
 * Filter-chain errors never reach @RestControllerAdvice, so we format them here.
 */
@Component
public class SecurityErrorWriter {

    private final ObjectMapper objectMapper;

    public SecurityErrorWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse body = new ErrorResponse(status, message, LocalDateTime.now(), null);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
