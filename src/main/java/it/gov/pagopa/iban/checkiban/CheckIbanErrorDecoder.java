package it.gov.pagopa.iban.checkiban;

import feign.Response;
import feign.codec.ErrorDecoder;

public class CheckIbanErrorDecoder implements ErrorDecoder {
  @Override
  public Exception decode(String methodKey, Response response) {

    switch (response.status()) {
      case 400:
        return new Exception();
      case 501:
        return new Exception();
      case 502:
        return new Exception();
      default:
        return new Exception();
    }
  }

}
