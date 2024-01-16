package it.gov.pagopa.iban.exception;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.common.web.exception.ServiceExceptionPayload;
import it.gov.pagopa.iban.constants.IbanConstants;

public class CheckIbanInvocationException extends ServiceException {

  public CheckIbanInvocationException(String message, boolean printStackTrace, Throwable ex) {
    this(IbanConstants.ExceptionCode.GENERIC_ERROR, message, null, printStackTrace, ex);
  }

  public CheckIbanInvocationException(String code, String message) {
    this(code, message, null, false, null);
  }

  public CheckIbanInvocationException(String code, String message, ServiceExceptionPayload payload, boolean printStackTrace, Throwable ex) {
    super(code, message, payload, printStackTrace, ex);
  }
}
