package it.gov.pagopa.iban.checkiban;

import it.gov.pagopa.iban.dto.CheckIbanDTO;
import it.gov.pagopa.iban.model.CheckIbanResource;
import javax.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(name = "${rest-client.checkiban.serviceCode}", url = "${rest-client.checkiban.base-url}")

public interface CheckIbanRestClient {
  @PostMapping(value = "${rest-client.checkiban.url}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  CheckIbanResource checkIban(@RequestBody @Valid CheckIbanDTO checkIbanDTO,
      @RequestHeader("apikey") String apikey,
      @RequestHeader("Auth-Schema") String authSchema);
}
