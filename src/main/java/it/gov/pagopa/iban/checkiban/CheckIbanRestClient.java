package it.gov.pagopa.iban.checkiban;

import it.gov.pagopa.iban.dto.RequestCheckIbanDTO;
import it.gov.pagopa.iban.dto.ResponseCheckIbanDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(name = "${rest-client.checkiban.serviceCode}", url = "${rest-client.checkiban.base-url}")

public interface CheckIbanRestClient {
  @PostMapping(value = "${rest-client.checkiban.url}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  ResponseEntity<ResponseCheckIbanDTO> checkIban(@RequestBody @Valid RequestCheckIbanDTO requestCheckIbanDTO,
                                                @RequestHeader("apikey") String apikey,
                                                @RequestHeader("Auth-Schema") String authSchema);
}
