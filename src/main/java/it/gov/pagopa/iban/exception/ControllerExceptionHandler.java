package it.gov.pagopa.iban.exception;

import it.gov.pagopa.iban.dto.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler({IbanException.class})
    public ResponseEntity<ErrorDTO> handleException(IbanException ex) {
        return new ResponseEntity<>(new ErrorDTO(ex.getCode(), ex.getMessage()),
                HttpStatus.valueOf(ex.getCode()));
    }
}