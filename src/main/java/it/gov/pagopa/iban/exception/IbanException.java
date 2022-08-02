package it.gov.pagopa.iban.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class IbanException extends RuntimeException{
  private final int code;
  private final String message;

}
