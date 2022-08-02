package it.gov.pagopa.iban.service;

import it.gov.pagopa.iban.dto.IbanDTO;
import it.gov.pagopa.iban.dto.IbanQueueDTO;
import java.util.List;

public interface IbanService {
    List<IbanDTO> getIbanList(String userId);
    void saveIban(IbanQueueDTO iban);
    IbanDTO getIban(String iban, String userId);
}
