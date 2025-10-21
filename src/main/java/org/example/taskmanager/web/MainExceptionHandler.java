package org.example.taskmanager.web;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class MainExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(MainExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> GenericExceptionHandler(Exception ex){
        log.error("Handle exception: ", ex);
        var errorDto = new ErrorResponseDto(
                "Internal server error",
                ex.getMessage(),
                LocalDateTime.now()
                );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorDto);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> EntityNotFoundExceptionHandler (EntityNotFoundException ex) {
        log.error("Handle EntityNotFoundException: ", ex);
        var errorDto = new ErrorResponseDto(
                "Object not found",
                ex.getMessage(),
                LocalDateTime.now()
                );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorDto);
    }

    @ExceptionHandler(exception = {
            IllegalArgumentException.class,
            IllegalStateException.class,
            MethodArgumentNotValidException.class
            //ConstraintViolationException.class
    })
    public ResponseEntity<ErrorResponseDto> BadRequestHandler (Exception ex) {
        log.error("Handle BadRequest: ", ex);
        var errorDto = new ErrorResponseDto(
                "Bad request",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }
}


