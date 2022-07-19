package it.gov.pagopa.iban.exception;

import feign.Response;
import org.springframework.http.HttpStatus;

public class UnknowPSPException extends RuntimeException{
  private String reason;
  private String error;
  private HttpStatus httpStatus;
  public UnknowPSPException(Response response) {
    this.reason= response.reason();
    this.error="generic.error";
    this.httpStatus=HttpStatus.INTERNAL_SERVER_ERROR;
  }
}
