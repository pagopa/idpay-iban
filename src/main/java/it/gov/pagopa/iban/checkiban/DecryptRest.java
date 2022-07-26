package it.gov.pagopa.iban.checkiban;

import it.gov.pagopa.iban.dto.DecryptedCfDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "${rest-client.decrypt.cf}", url = "${rest-client.decrypt.base-url}")
public interface DecryptRest {

  @GetMapping(value= "/tokens/{token}/pii", produces = MediaType.APPLICATION_JSON_VALUE)
  DecryptedCfDTO getPiiByToken(@RequestParam("token") String token);
}
