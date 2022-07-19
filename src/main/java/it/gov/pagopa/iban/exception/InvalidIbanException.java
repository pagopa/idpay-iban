package it.gov.pagopa.iban.exception;

import feign.Response;
import org.springframework.http.HttpStatus;

public class InvalidIbanException extends RuntimeException{
  private String reason;
  private String error;
  private HttpStatus httpStatus;
  public InvalidIbanException() {
    this.reason= "Invalid IBAN";
    this.error="resource.invalid.error";
    this.httpStatus=HttpStatus.BAD_REQUEST;
  }
}
