package it.gov.pagopa.iban.exception;

import feign.Response;
import org.springframework.http.HttpStatus;

public class UnknowPSPTimeoutException extends RuntimeException{
  private String reason;
  private String error;
  private HttpStatus httpStatus;
  public UnknowPSPTimeoutException(Response response) {
    this.reason= "UNKNOW PSP";
    this.error="psp.unknow.error";
    this.httpStatus=HttpStatus.NOT_IMPLEMENTED;
  }
}
