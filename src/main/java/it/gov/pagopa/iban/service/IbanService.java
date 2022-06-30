package it.gov.pagopa.iban.service;

import it.gov.pagopa.iban.dto.IbanDTO;

public interface IbanService {
    void putIban(String initiativeId, String userId, String iban, String description);
    IbanDTO getIban(String initiativeId, String userId);
}
