package it.gov.pagopa.iban.checkiban;

import it.gov.pagopa.iban.dto.ResponseCheckIbanDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface CheckIbanRestConnector {
  ResponseEntity<ResponseCheckIbanDTO> checkIban(String payOffInstr, String fiscalCode);
}
