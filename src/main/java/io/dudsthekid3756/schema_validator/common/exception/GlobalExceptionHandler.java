package io.dudsthekid3756.schema_validator.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.Timestamp;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Method to build new exception entity for generic Server Error
     *
     * @param e       an instance of exception
     * @param request the initial request that caused the exception to be thrown
     * @return a new ResponseEntity of exception
     */
    @ExceptionHandler(ServerError.class)
    public ResponseEntity<ExceptionResponse> serverError(ServerError e, HttpServletRequest request) {
        ExceptionResponse response = ExceptionResponse.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .error("An unknown error occurred with the server.")
                .path(request.getRequestURI())
                .timestamp(Timestamp.from(Instant.now()))
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Method to build new exception entity for Bad Request
     *
     * @param e       an instance of exception
     * @param request the initial request that caused the exception to be thrown
     * @return a new ResponseEntity of exception
     */
    @ExceptionHandler(BadRequest.class)
    public ResponseEntity<ExceptionResponse> badRequest(BadRequest e, HttpServletRequest request) {
        ExceptionResponse response = ExceptionResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .status(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .error("One or more of the provided values is invalid.")
                .path(request.getRequestURI())
                .timestamp(Timestamp.from(Instant.now()))
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Method to build new exception entity for Not Found
     *
     * @param e       an instance of exception
     * @param request the initial request that caused the exception to be thrown
     * @return a new ResponseEntity of exception
     */
    @ExceptionHandler(NotFound.class)
    public ResponseEntity<ExceptionResponse> notFound(NotFound e, HttpServletRequest request) {
        ExceptionResponse response = ExceptionResponse.builder()
                .code(HttpStatus.NOT_FOUND.value())
                .status(HttpStatus.NOT_FOUND.getReasonPhrase())
                .error("Requested resource was not found.")
                .path(request.getRequestURI())
                .timestamp(Timestamp.from(Instant.now()))
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}
