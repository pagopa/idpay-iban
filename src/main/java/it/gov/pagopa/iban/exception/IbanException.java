package it.gov.pagopa.iban.exception;

import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@SuppressWarnings("squid:S110")
public class IbanException extends ClientExceptionWithBody {

  public IbanException(Integer code, String message) {
    super(HttpStatus.valueOf(code), code, message, null);
  }

}
