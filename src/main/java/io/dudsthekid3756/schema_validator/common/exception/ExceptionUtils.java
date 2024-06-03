package io.dudsthekid3756.schema_validator.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Objects;

@Component
@AllArgsConstructor
@Slf4j
public class ExceptionUtils {

    private final GlobalExceptionHandler globalExceptionHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void handleException(HttpServletResponse response, BadRequest e, HttpServletRequest wrappedRequest) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        ExceptionResponse exceptionResponseBody = globalExceptionHandler.badRequest(e, wrappedRequest).getBody();
        Objects.requireNonNull(exceptionResponseBody)
                .setError("An error occurred while validating the schema with OAS");

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ"));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper
                .valueToTree(exceptionResponseBody)
                .toPrettyString()
        );
    }

    public void handleException(HttpServletResponse response, ServerError e, HttpServletRequest wrappedRequest) throws IOException {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

        ExceptionResponse exceptionResponseBody = globalExceptionHandler.serverError(e, wrappedRequest).getBody();
        Objects.requireNonNull(exceptionResponseBody)
                .setError("An error occurred while validating the schema with OAS");

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ"));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper
                .valueToTree(exceptionResponseBody)
                .toPrettyString()
        );
    }
}
