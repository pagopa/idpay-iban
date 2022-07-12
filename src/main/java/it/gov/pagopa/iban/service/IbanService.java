package it.gov.pagopa.iban.service;

import it.gov.pagopa.iban.dto.IbanQueueDTO;
import java.util.List;

public interface IbanService {
    List<IbanQueueDTO> getIbanList(String userId);
    void saveIban(IbanQueueDTO iban);
}
