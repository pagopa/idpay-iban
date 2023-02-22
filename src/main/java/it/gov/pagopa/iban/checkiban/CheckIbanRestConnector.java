package it.gov.pagopa.iban.checkiban;

import it.gov.pagopa.iban.dto.ResponseCheckIbanDTO;
import org.springframework.stereotype.Service;

@Service
public interface CheckIbanRestConnector {
  ResponseCheckIbanDTO checkIban(String payOffInstr, String fiscalCode);
}
