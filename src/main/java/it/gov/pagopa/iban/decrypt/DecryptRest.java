package it.gov.pagopa.iban.decrypt;

import it.gov.pagopa.iban.dto.DecryptedCfDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(name = "${rest-client.decrypt.cf}", url = "${rest-client.decrypt.base-url}")
public interface DecryptRest {

  @GetMapping(value = "/tokens/{token}/pii", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  DecryptedCfDTO getPiiByToken(@PathVariable("token") String token,
      @RequestHeader("x-api-key") String apikey);
}
