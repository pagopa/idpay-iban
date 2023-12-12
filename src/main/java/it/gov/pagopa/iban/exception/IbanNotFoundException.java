package it.gov.pagopa.iban.exception;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.iban.constants.IbanConstants;

public class IbanNotFoundException extends ServiceException {

    public IbanNotFoundException(String message) {
        this(IbanConstants.ExceptionCode.IBAN_NOT_FOUND, message);
    }

    public IbanNotFoundException(String code, String message) {
        this(code, message, false, null);
    }

    public IbanNotFoundException(String code, String message, boolean printStackTrace, Throwable ex) {
        super(code, message, printStackTrace, ex);
    }

}
