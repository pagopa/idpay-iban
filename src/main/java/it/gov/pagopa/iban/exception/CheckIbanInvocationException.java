package it.gov.pagopa.iban.exception;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.iban.constants.IbanConstants;

public class CheckIbanInvocationException extends ServiceException {

  public CheckIbanInvocationException(String message) {
    this(IbanConstants.ExceptionCode.GENERIC_ERROR, message);
  }

  public CheckIbanInvocationException(String code, String message) {
    this(code, message, false, null);
  }

  public CheckIbanInvocationException(String code, String message, boolean printStackTrace, Throwable ex) {
    super(code, message, printStackTrace, ex);
  }
}
