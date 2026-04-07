package com.is.in_studio.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class StatusExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<String> violations = ex.getFieldErrors().stream()
            .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
            .toList();

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Validation failed for one or more fields"
        );
        problem.setProperty("violations", violations);
        return problem;
    }

    @ExceptionHandler(CustomExceptions.class)
    public ProblemDetail handleCustomResponseException(CustomExceptions ex) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        if (ex.getClass() == CustomExceptions.NotFoundException.class) {
            httpStatus = HttpStatus.NOT_FOUND;
        } else if (ex.getClass() == CustomExceptions.ServerErrorException.class) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return ProblemDetail.forStatusAndDetail(httpStatus, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception ex) {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred. Please try again later."
        );
    }
}
