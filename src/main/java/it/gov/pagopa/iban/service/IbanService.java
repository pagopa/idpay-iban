package it.gov.pagopa.iban.service;

import it.gov.pagopa.iban.dto.IbanDTO;
import it.gov.pagopa.iban.dto.IbanListDTO;
import it.gov.pagopa.iban.dto.IbanQueueDTO;

public interface IbanService {
    IbanListDTO getIbanList(String userId);
    void saveIban(IbanQueueDTO iban);
    IbanDTO getIban(String iban, String userId);
}
