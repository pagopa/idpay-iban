package it.gov.pagopa.iban.checkiban;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "${rest-client.checkiban.serviceCode}", url = "${rest-client.checkiban.base-url}")

public interface CheckIbanRestClientMock {

  @PostMapping(value = "${rest-client.checkiban.url}", consumes = MediaType.APPLICATION_JSON_VALUE)
  String checkIban();
}
