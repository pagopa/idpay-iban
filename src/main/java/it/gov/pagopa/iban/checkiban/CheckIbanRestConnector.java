package it.gov.pagopa.iban.checkiban;

import it.gov.pagopa.iban.dto.CheckIbanDTO;
import org.springframework.stereotype.Service;

@Service
public interface CheckIbanRestConnector {
  CheckIbanDTO checkIban(String payOffInstr, String fiscalCode);
}
