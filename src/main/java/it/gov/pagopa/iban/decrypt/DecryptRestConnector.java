package it.gov.pagopa.iban.decrypt;

import it.gov.pagopa.iban.dto.DecryptedCfDTO;
import org.springframework.stereotype.Service;

@Service
public interface DecryptRestConnector {

  DecryptedCfDTO getPiiByToken(String token);
}
