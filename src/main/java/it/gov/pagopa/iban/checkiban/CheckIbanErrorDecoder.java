package it.gov.pagopa.iban.checkiban;

import feign.Response;
import feign.codec.ErrorDecoder;
import it.gov.pagopa.iban.exception.CheckIbanException;
import it.gov.pagopa.iban.exception.InvalidIbanException;
import it.gov.pagopa.iban.exception.UnknowPSPException;
import it.gov.pagopa.iban.exception.UnknowPSPTimeoutException;

public class CheckIbanErrorDecoder implements ErrorDecoder {

  @Override
  public Exception decode(String methodKey, Response response) {

    switch (response.status()) {
      case 400:
        return new InvalidIbanException();
      case 501:
        return new UnknowPSPException(response);
      case 502:
        return new UnknowPSPTimeoutException(response);
      default:
        return new CheckIbanException(response);
    }
  }


}