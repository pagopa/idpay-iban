package it.gov.pagopa.iban.config;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.iban.exception.CheckIbanInvocationException;
import it.gov.pagopa.iban.exception.IbanNotFoundException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ServiceExceptionConfig {

  @Bean
  public Map<Class<? extends ServiceException>, HttpStatus> serviceExceptionMapper() {
    Map<Class<? extends ServiceException>, HttpStatus> exceptionMap = new HashMap<>();

    // NotFound
    exceptionMap.put(IbanNotFoundException.class, HttpStatus.NOT_FOUND);

    // InternalServerError
    exceptionMap.put(CheckIbanInvocationException.class, HttpStatus.INTERNAL_SERVER_ERROR);

    return exceptionMap;
  }

}
